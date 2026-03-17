package com.qizhi.continueeducation.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qizhi.continueeducation.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model_config")
public class AiModelConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String providerName;
    private String displayName;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Integer enabled;
    private String remark;
}
