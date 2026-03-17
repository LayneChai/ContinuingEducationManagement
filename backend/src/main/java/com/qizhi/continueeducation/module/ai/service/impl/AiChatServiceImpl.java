package com.qizhi.continueeducation.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qizhi.continueeducation.module.ai.client.DeepSeekChatMessage;
import com.qizhi.continueeducation.module.ai.client.DeepSeekChatRequest;
import com.qizhi.continueeducation.module.ai.client.DeepSeekChatResponse;
import com.qizhi.continueeducation.module.ai.dto.AiChatRequest;
import com.qizhi.continueeducation.module.ai.entity.AiChatMessage;
import com.qizhi.continueeducation.module.ai.entity.AiChatSession;
import com.qizhi.continueeducation.module.ai.mapper.AiChatMessageMapper;
import com.qizhi.continueeducation.module.ai.mapper.AiChatSessionMapper;
import com.qizhi.continueeducation.module.ai.service.AiChatService;
import com.qizhi.continueeducation.module.ai.service.AiModelConfigService;
import com.qizhi.continueeducation.module.ai.vo.AiChatMessageVo;
import com.qizhi.continueeducation.module.ai.vo.AiChatSessionVo;
import com.qizhi.continueeducation.module.ai.vo.AiRuntimeConfig;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final AiModelConfigService aiModelConfigService;
    private final EduCourseMapper courseMapper;
    private final SysUserMapper userMapper;

    @Override
    public Flux<ServerSentEvent<String>> streamReply(Long userId, AiChatRequest request) {
        return Flux.create(sink -> {
            AtomicInteger eventId = new AtomicInteger(0);
            long start = System.currentTimeMillis();
            AiChatSession session = null;
            AiChatMessage userMessage = null;
            try {
                session = prepareSession(userId, request);
                userMessage = saveUserMessage(userId, request, session);
                emit(sink, eventId, "session", String.valueOf(session.getId()));

                AiRuntimeConfig runtimeConfig = aiModelConfigService.getActiveRuntimeConfig();

                if (runtimeConfig.getApiKey() == null || runtimeConfig.getApiKey().isBlank()) {
                    String fallback = "当前未配置可用的大模型密钥，请先在管理员端配置并启用模型，或设置环境变量 DEEPSEEK_API_KEY。";
                    persistAssistantMessage(session, userId, request.getCourseId(), fallback, runtimeConfig.getModelName(), (int) (System.currentTimeMillis() - start));
                    emit(sink, eventId, "message", fallback);
                    emit(sink, eventId, "done", "[DONE]");
                    sink.complete();
                    return;
                }

                List<DeepSeekChatMessage> messages = buildRequestMessages(session.getId(), request.getCourseId(), request.getMessage());
                DeepSeekChatRequest deepSeekRequest = DeepSeekChatRequest.builder()
                        .model(runtimeConfig.getModelName())
                        .messages(messages)
                        .stream(true)
                        .temperature(0.7)
                        .build();

                Request httpRequest = new Request.Builder()
                        .url(normalizeBaseUrl(runtimeConfig.getBaseUrl()) + "/chat/completions")
                        .addHeader("Authorization", "Bearer " + runtimeConfig.getApiKey())
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(objectMapper.writeValueAsString(deepSeekRequest), JSON))
                        .build();

                StringBuilder assistantContent = new StringBuilder();
                try (Response response = okHttpClient.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() == null ? "" : response.body().string();
                        throw new IllegalStateException("DeepSeek 调用失败: HTTP " + response.code() + " " + errorBody);
                    }
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IllegalStateException("DeepSeek 返回内容为空");
                    }
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isBlank() || !line.startsWith("data:")) {
                                continue;
                            }
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            DeepSeekChatResponse chunk = objectMapper.readValue(data, DeepSeekChatResponse.class);
                            if (chunk.getChoices() == null || chunk.getChoices().isEmpty()) {
                                continue;
                            }
                            DeepSeekChatResponse.Choice choice = chunk.getChoices().get(0);
                            String content = choice.getDelta() == null ? null : choice.getDelta().getContent();
                            if (content == null || content.isEmpty()) {
                                continue;
                            }
                            assistantContent.append(content);
                            emit(sink, eventId, "message", content);
                        }
                    }
                }

                String finalContent = assistantContent.toString();
                if (finalContent.isBlank()) {
                    finalContent = "AI 暂时没有返回内容，请稍后重试。";
                    emit(sink, eventId, "message", finalContent);
                }
                persistAssistantMessage(session, userId, request.getCourseId(), finalContent, runtimeConfig.getModelName(), (int) (System.currentTimeMillis() - start));
                emit(sink, eventId, "done", "[DONE]");
                sink.complete();
            } catch (Exception ex) {
                log.error("AI chat failed", ex);
                String error = ex.getMessage() == null ? "AI 服务暂不可用" : ex.getMessage();
                if (session != null) {
                    persistAssistantMessage(session, userId, request.getCourseId(), "[ERROR] " + error, resolveModelName(), (int) (System.currentTimeMillis() - start));
                }
                emit(sink, eventId, "error", error);
                sink.complete();
            }
        });
    }

    @Override
    public List<AiChatSessionVo> studentSessions(Long studentId, Long courseId) {
        List<AiChatSession> sessions = sessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getUserId, studentId)
                .eq(courseId != null, AiChatSession::getCourseId, courseId)
                .orderByDesc(AiChatSession::getLastMessageTime)
                .orderByDesc(AiChatSession::getId));
        return toSessionVos(sessions);
    }

    @Override
    public List<AiChatMessageVo> studentMessages(Long studentId, Long sessionId) {
        AiChatSession session = requireSession(sessionId);
        if (!studentId.equals(session.getUserId())) {
            throw new IllegalArgumentException("无权查看该会话");
        }
        return toMessageVos(sessionId);
    }

    @Override
    public List<AiChatSessionVo> teacherSessions(Long teacherId, Long courseId) {
        List<Long> courseIds = courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                        .select(EduCourse::getId)
                        .eq(EduCourse::getTeacherId, teacherId)
                        .eq(courseId != null, EduCourse::getId, courseId))
                .stream().map(EduCourse::getId).toList();
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        return toSessionVos(sessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                .in(AiChatSession::getCourseId, courseIds)
                .orderByDesc(AiChatSession::getLastMessageTime)
                .orderByDesc(AiChatSession::getId)));
    }

    @Override
    public List<AiChatMessageVo> teacherMessages(Long teacherId, Long sessionId) {
        AiChatSession session = requireSession(sessionId);
        EduCourse course = courseMapper.selectById(session.getCourseId());
        if (course == null || !teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权查看该会话记录");
        }
        return toMessageVos(sessionId);
    }

    private AiChatSession prepareSession(Long userId, AiChatRequest request) {
        EduCourse course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        AiChatSession session;
        if (request.getSessionId() == null) {
            session = new AiChatSession();
            session.setUserId(userId);
            session.setCourseId(request.getCourseId());
            session.setTitle(buildSessionTitle(request.getMessage(), course.getTitle()));
            session.setStatus(1);
            session.setMessageCount(0);
            session.setTotalTokens(0);
            session.setLastMessageTime(LocalDateTime.now());
            sessionMapper.insert(session);
        } else {
            session = requireSession(request.getSessionId());
            if (!userId.equals(session.getUserId())) {
                throw new IllegalArgumentException("无权使用该会话");
            }
        }
        return session;
    }

    private AiChatMessage saveUserMessage(Long userId, AiChatRequest request, AiChatSession session) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(userId);
        message.setCourseId(request.getCourseId());
        message.setMessageRole("user");
        message.setContent(request.getMessage());
        message.setModelName(resolveModelName());
        message.setTokenCount(0);
        messageMapper.insert(message);
        session.setLastMessageTime(LocalDateTime.now());
        session.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + 1);
        sessionMapper.updateById(session);
        return message;
    }

    private void persistAssistantMessage(AiChatSession session, Long userId, Long courseId, String content, String modelName, int responseTimeMs) {
        AiChatMessage assistant = new AiChatMessage();
        assistant.setSessionId(session.getId());
        assistant.setUserId(userId);
        assistant.setCourseId(courseId);
        assistant.setMessageRole("assistant");
        assistant.setContent(content);
        assistant.setModelName(modelName);
        assistant.setTokenCount(0);
        assistant.setResponseTimeMs(responseTimeMs);
        messageMapper.insert(assistant);
        session.setLastMessageTime(LocalDateTime.now());
        session.setMessageCount((session.getMessageCount() == null ? 0 : session.getMessageCount()) + 1);
        sessionMapper.updateById(session);
    }

    private List<DeepSeekChatMessage> buildRequestMessages(Long sessionId, Long courseId, String userMessage) {
        EduCourse course = courseMapper.selectById(courseId);
        String systemPrompt = "你是启智继续教育管理系统中的课程AI辅导助手。"
                + "请优先围绕当前课程内容回答，输出使用简体中文，回答准确、简洁、友好。"
                + "如果问题明显超出课程范围，请先说明，并尽量结合课程学习场景给出帮助。"
                + "当前课程标题：" + (course == null ? "未知课程" : course.getTitle()) + "。"
                + "课程简介：" + (course == null || course.getDescription() == null ? "暂无简介" : course.getDescription()) + "。";

        List<AiChatMessage> histories = messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .orderByDesc(AiChatMessage::getId)
                .last("limit 20"));
        Collections.reverse(histories);

        List<DeepSeekChatMessage> messages = histories.stream()
                .filter(item -> Objects.equals(item.getMessageRole(), "user") || Objects.equals(item.getMessageRole(), "assistant"))
                .map(item -> new DeepSeekChatMessage(item.getMessageRole(), item.getContent()))
                .collect(Collectors.toList());
        messages.add(0, new DeepSeekChatMessage("system", systemPrompt));
        if (messages.stream().noneMatch(item -> "user".equals(item.getRole()) && userMessage.equals(item.getContent()))) {
            messages.add(new DeepSeekChatMessage("user", userMessage));
        }
        return messages;
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        String baseUrl = rawBaseUrl;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!baseUrl.endsWith("/v1")) {
            baseUrl = baseUrl + "/v1";
        }
        return baseUrl;
    }

    private String resolveModelName() {
        return aiModelConfigService.getActiveRuntimeConfig().getModelName();
    }

    private void emit(reactor.core.publisher.FluxSink<ServerSentEvent<String>> sink, AtomicInteger eventId, String event, String data) {
        sink.next(ServerSentEvent.<String>builder()
                .id(String.valueOf(eventId.incrementAndGet()))
                .event(event)
                .data(data)
                .build());
    }

    private String buildSessionTitle(String message, String courseTitle) {
        String plain = message == null ? "新对话" : message.trim();
        if (plain.length() > 20) {
            plain = plain.substring(0, 20);
        }
        return (courseTitle == null ? "AI对话" : courseTitle) + " - " + plain;
    }

    private AiChatSession requireSession(Long sessionId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        return session;
    }

    private List<AiChatSessionVo> toSessionVos(List<AiChatSession> sessions) {
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduCourse> courseMap = courseMapper.selectBatchIds(sessions.stream().map(AiChatSession::getCourseId).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourse::getId, Function.identity(), (a, b) -> a));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(sessions.stream().map(AiChatSession::getUserId).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        return sessions.stream().map(item -> AiChatSessionVo.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .userName(userMap.get(item.getUserId()) == null ? null : userMap.get(item.getUserId()).getRealName())
                .courseId(item.getCourseId())
                .courseTitle(courseMap.get(item.getCourseId()) == null ? null : courseMap.get(item.getCourseId()).getTitle())
                .title(item.getTitle())
                .status(item.getStatus())
                .messageCount(item.getMessageCount())
                .totalTokens(item.getTotalTokens())
                .lastMessageTime(item.getLastMessageTime())
                .createTime(item.getCreateTime())
                .build())
                .toList();
    }

    private List<AiChatMessageVo> toMessageVos(Long sessionId) {
        return messageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByAsc(AiChatMessage::getId))
                .stream()
                .map(item -> AiChatMessageVo.builder()
                        .id(item.getId())
                        .sessionId(item.getSessionId())
                        .role(item.getMessageRole())
                        .content(item.getContent())
                        .modelName(item.getModelName())
                        .tokenCount(item.getTokenCount())
                        .createTime(item.getCreateTime())
                        .build())
                .toList();
    }
}
