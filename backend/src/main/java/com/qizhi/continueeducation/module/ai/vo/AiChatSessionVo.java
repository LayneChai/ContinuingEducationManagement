package com.qizhi.continueeducation.module.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiChatSessionVo {
    private Long id;
    private Long userId;
    private String userName;
    private Long courseId;
    private String courseTitle;
    private String title;
    private Integer status;
    private Integer messageCount;
    private Integer totalTokens;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
}
