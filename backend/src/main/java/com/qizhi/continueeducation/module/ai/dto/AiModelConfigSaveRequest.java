package com.qizhi.continueeducation.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiModelConfigSaveRequest {

    @NotBlank(message = "服务商名称不能为空")
    private String providerName;
    @NotBlank(message = "显示名称不能为空")
    private String displayName;
    @NotBlank(message = "接口地址不能为空")
    private String baseUrl;
    @NotBlank(message = "API Key不能为空")
    private String apiKey;
    @NotBlank(message = "模型名称不能为空")
    private String modelName;
    private String remark;
}
