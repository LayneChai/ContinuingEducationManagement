package com.qizhi.continueeducation.module.ai.client;

import lombok.Data;

import java.util.List;

@Data
public class DeepSeekChatResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Delta delta;
        private DeepSeekChatMessage message;
        private String finishReason;
    }

    @Data
    public static class Delta {
        private String content;
        private String role;
    }
}
