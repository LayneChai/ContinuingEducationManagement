package com.qizhi.continueeducation.module.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.ai.dto.AiChatRequest;
import com.qizhi.continueeducation.module.ai.service.AiChatService;
import com.qizhi.continueeducation.module.ai.vo.AiChatMessageVo;
import com.qizhi.continueeducation.module.ai.vo.AiChatSessionVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping(value = "/api/student/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@Valid @RequestBody AiChatRequest request) {
        StpUtil.checkRole(RoleCode.STUDENT);
        SseEmitter emitter = new SseEmitter(0L);
        aiChatService.streamReply(StpUtil.getLoginIdAsLong(), request)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(event -> sendEvent(emitter, event), emitter::completeWithError, emitter::complete);
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, ServerSentEvent<String> event) {
        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event();
            if (event.id() != null) {
                builder.id(event.id());
            }
            if (event.event() != null) {
                builder.name(event.event());
            }
            builder.data(event.data() == null ? "" : event.data());
            emitter.send(builder);
        } catch (Exception ex) {
            emitter.completeWithError(ex);
        }
    }

    @GetMapping("/api/student/ai/sessions")
    public ApiResponse<List<AiChatSessionVo>> studentSessions(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(aiChatService.studentSessions(StpUtil.getLoginIdAsLong(), courseId));
    }

    @GetMapping("/api/student/ai/sessions/{sessionId}/messages")
    public ApiResponse<List<AiChatMessageVo>> studentMessages(@PathVariable Long sessionId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(aiChatService.studentMessages(StpUtil.getLoginIdAsLong(), sessionId));
    }

    @GetMapping("/api/teacher/ai/sessions")
    public ApiResponse<List<AiChatSessionVo>> teacherSessions(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(aiChatService.teacherSessions(StpUtil.getLoginIdAsLong(), courseId));
    }

    @GetMapping("/api/teacher/ai/sessions/{sessionId}/messages")
    public ApiResponse<List<AiChatMessageVo>> teacherMessages(@PathVariable Long sessionId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(aiChatService.teacherMessages(StpUtil.getLoginIdAsLong(), sessionId));
    }
}
