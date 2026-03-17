package com.qizhi.continueeducation.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qizhi.continueeducation.config.properties.DeepSeekProperties;
import com.qizhi.continueeducation.module.ai.client.DeepSeekChatMessage;
import com.qizhi.continueeducation.module.ai.client.DeepSeekChatRequest;
import com.qizhi.continueeducation.module.ai.dto.AiModelConfigSaveRequest;
import com.qizhi.continueeducation.module.ai.entity.AiModelConfig;
import com.qizhi.continueeducation.module.ai.mapper.AiModelConfigMapper;
import com.qizhi.continueeducation.module.ai.service.AiModelConfigService;
import com.qizhi.continueeducation.module.ai.vo.AiModelConfigVo;
import com.qizhi.continueeducation.module.ai.vo.AiModelTestResultVo;
import com.qizhi.continueeducation.module.ai.vo.AiRuntimeConfig;
import lombok.RequiredArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiModelConfigServiceImpl implements AiModelConfigService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final AiModelConfigMapper aiModelConfigMapper;
    private final DeepSeekProperties deepSeekProperties;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<AiModelConfigVo> listConfigs() {
        return aiModelConfigMapper.selectList(new LambdaQueryWrapper<AiModelConfig>()
                        .orderByDesc(AiModelConfig::getEnabled)
                        .orderByDesc(AiModelConfig::getUpdateTime)
                        .orderByDesc(AiModelConfig::getId))
                .stream()
                .map(this::toVo)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConfig(AiModelConfigSaveRequest request) {
        AiModelConfig config = new AiModelConfig();
        fillConfig(config, request);
        config.setEnabled(0);
        aiModelConfigMapper.insert(config);
        return config.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Long configId, AiModelConfigSaveRequest request) {
        AiModelConfig config = requireConfig(configId);
        fillConfig(config, request);
        aiModelConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableConfig(Long configId) {
        requireConfig(configId);
        aiModelConfigMapper.update(null, Wrappers.<AiModelConfig>lambdaUpdate().set(AiModelConfig::getEnabled, 0));
        AiModelConfig config = new AiModelConfig();
        config.setId(configId);
        config.setEnabled(1);
        aiModelConfigMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableConfig(Long configId) {
        AiModelConfig config = requireConfig(configId);
        if (config.getEnabled() == null || config.getEnabled() != 1) {
            return;
        }
        AiModelConfig update = new AiModelConfig();
        update.setId(configId);
        update.setEnabled(0);
        aiModelConfigMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long configId) {
        AiModelConfig config = requireConfig(configId);
        if (config.getEnabled() != null && config.getEnabled() == 1) {
            throw new IllegalStateException("请先停用当前模型，再执行删除");
        }
        aiModelConfigMapper.deleteById(config.getId());
    }

    @Override
    public AiModelTestResultVo testConfig(Long configId) {
        AiModelConfig config = requireConfig(configId);
        try {
            DeepSeekChatRequest requestPayload = DeepSeekChatRequest.builder()
                    .model(config.getModelName())
                    .messages(List.of(new DeepSeekChatMessage("user", "请回复：连接测试成功")))
                    .stream(false)
                    .temperature(0.2)
                    .build();

            Request request = new Request.Builder()
                    .url(normalizeBaseUrl(config.getBaseUrl()) + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + config.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(objectMapper.writeValueAsString(requestPayload), JSON))
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String body = response.body() == null ? "" : response.body().string();
                    return AiModelTestResultVo.builder()
                            .success(false)
                            .message("连接失败: HTTP " + response.code() + " " + body)
                            .modelName(config.getModelName())
                            .providerName(config.getProviderName())
                            .build();
                }
                String body = response.body() == null ? "" : response.body().string();
                JsonNode jsonNode = objectMapper.readTree(body);
                String content = jsonNode.path("choices").isArray() && jsonNode.path("choices").size() > 0
                        ? jsonNode.path("choices").get(0).path("message").path("content").asText("")
                        : "";
                return AiModelTestResultVo.builder()
                        .success(true)
                        .message(content.isBlank() ? "连接测试成功" : content)
                        .modelName(config.getModelName())
                        .providerName(config.getProviderName())
                        .build();
            }
        } catch (Exception ex) {
            return AiModelTestResultVo.builder()
                    .success(false)
                    .message(ex.getMessage() == null ? "连接测试失败" : ex.getMessage())
                    .modelName(config.getModelName())
                    .providerName(config.getProviderName())
                    .build();
        }
    }

    @Override
    public AiRuntimeConfig getActiveRuntimeConfig() {
        AiModelConfig config = aiModelConfigMapper.selectOne(new LambdaQueryWrapper<AiModelConfig>()
                .eq(AiModelConfig::getEnabled, 1)
                .orderByDesc(AiModelConfig::getUpdateTime)
                .last("limit 1"));
        if (config != null) {
            return AiRuntimeConfig.builder()
                    .providerName(config.getProviderName())
                    .displayName(config.getDisplayName())
                    .baseUrl(config.getBaseUrl())
                    .apiKey(config.getApiKey())
                    .modelName(config.getModelName())
                    .build();
        }
        return AiRuntimeConfig.builder()
                .providerName("DeepSeek")
                .displayName("环境变量默认模型")
                .baseUrl(deepSeekProperties.getBaseUrl())
                .apiKey(deepSeekProperties.getApiKey())
                .modelName(deepSeekProperties.getModel())
                .build();
    }

    private void fillConfig(AiModelConfig config, AiModelConfigSaveRequest request) {
        config.setProviderName(request.getProviderName());
        config.setDisplayName(request.getDisplayName());
        config.setBaseUrl(request.getBaseUrl());
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            config.setApiKey(request.getApiKey());
        }
        config.setModelName(request.getModelName());
        config.setRemark(request.getRemark());
    }

    private String normalizeBaseUrl(String rawBaseUrl) {
        String baseUrl = rawBaseUrl;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (!baseUrl.endsWith("/v1")) {
            baseUrl = baseUrl + "/v1";
        }
        return baseUrl;
    }

    private AiModelConfig requireConfig(Long configId) {
        AiModelConfig config = aiModelConfigMapper.selectById(configId);
        if (config == null) {
            throw new IllegalArgumentException("模型配置不存在");
        }
        return config;
    }

    private AiModelConfigVo toVo(AiModelConfig config) {
        String key = config.getApiKey();
        String masked = (key == null || key.length() < 8) ? "****" : key.substring(0, 4) + "********" + key.substring(key.length() - 4);
        return AiModelConfigVo.builder()
                .id(config.getId())
                .providerName(config.getProviderName())
                .displayName(config.getDisplayName())
                .baseUrl(config.getBaseUrl())
                .apiKeyMasked(masked)
                .modelName(config.getModelName())
                .enabled(config.getEnabled())
                .remark(config.getRemark())
                .updateTime(config.getUpdateTime())
                .build();
    }
}
