package com.qizhi.continueeducation.module.ai.service;

import com.qizhi.continueeducation.module.ai.dto.AiChatRequest;
import com.qizhi.continueeducation.module.ai.vo.AiChatMessageVo;
import com.qizhi.continueeducation.module.ai.vo.AiChatSessionVo;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

import java.util.List;

public interface AiChatService {

    Flux<ServerSentEvent<String>> streamReply(Long userId, AiChatRequest request);

    List<AiChatSessionVo> studentSessions(Long studentId, Long courseId);

    List<AiChatMessageVo> studentMessages(Long studentId, Long sessionId);

    List<AiChatSessionVo> teacherSessions(Long teacherId, Long courseId);

    List<AiChatMessageVo> teacherMessages(Long teacherId, Long sessionId);
}
