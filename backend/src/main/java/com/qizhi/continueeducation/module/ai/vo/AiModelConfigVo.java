package com.qizhi.continueeducation.module.ai.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiModelConfigVo {
    private Long id;
    private String providerName;
    private String displayName;
    private String baseUrl;
    private String apiKeyMasked;
    private String modelName;
    private Integer enabled;
    private String remark;
    private LocalDateTime updateTime;
}
