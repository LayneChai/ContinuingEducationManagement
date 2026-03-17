package com.qizhi.continueeducation.module.ai.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiRuntimeConfig {
    private String providerName;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private String displayName;
}
