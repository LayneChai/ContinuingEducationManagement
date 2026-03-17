package com.qizhi.continueeducation.module.ai.service;

import com.qizhi.continueeducation.module.ai.dto.AiModelConfigSaveRequest;
import com.qizhi.continueeducation.module.ai.vo.AiModelConfigVo;
import com.qizhi.continueeducation.module.ai.vo.AiRuntimeConfig;
import com.qizhi.continueeducation.module.ai.vo.AiModelTestResultVo;

import java.util.List;

public interface AiModelConfigService {

    List<AiModelConfigVo> listConfigs();

    Long createConfig(AiModelConfigSaveRequest request);

    void updateConfig(Long configId, AiModelConfigSaveRequest request);

    void enableConfig(Long configId);

    void deleteConfig(Long configId);

    AiModelTestResultVo testConfig(Long configId);

    AiRuntimeConfig getActiveRuntimeConfig();
}
