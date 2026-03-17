package com.qizhi.continueeducation.module.ai.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiModelTestResultVo {
    private Boolean success;
    private String message;
    private String modelName;
    private String providerName;
}
