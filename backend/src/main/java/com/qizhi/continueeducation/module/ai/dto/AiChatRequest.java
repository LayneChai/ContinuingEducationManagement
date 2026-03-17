package com.qizhi.continueeducation.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiChatRequest {

    private Long sessionId;
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotBlank(message = "提问内容不能为空")
    private String message;
}
