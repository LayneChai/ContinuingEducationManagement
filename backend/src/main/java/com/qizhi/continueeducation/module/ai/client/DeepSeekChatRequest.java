package com.qizhi.continueeducation.module.ai.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DeepSeekChatRequest {
    private String model;
    private List<DeepSeekChatMessage> messages;
    private Boolean stream;
    private Double temperature;
}
