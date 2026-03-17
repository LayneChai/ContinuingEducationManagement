package com.qizhi.continueeducation.module.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiChatMessageVo {
    private Long id;
    private Long sessionId;
    private String role;
    private String content;
    private String modelName;
    private Integer tokenCount;
    private LocalDateTime createTime;
}
