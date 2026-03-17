package com.qizhi.continueeducation.module.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.ai.dto.AiModelConfigSaveRequest;
import com.qizhi.continueeducation.module.ai.service.AiModelConfigService;
import com.qizhi.continueeducation.module.ai.vo.AiModelConfigVo;
import com.qizhi.continueeducation.module.ai.vo.AiModelTestResultVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/ai-models")
public class AdminAiModelConfigController {

    private final AiModelConfigService aiModelConfigService;

    @GetMapping
    public ApiResponse<List<AiModelConfigVo>> list() {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(aiModelConfigService.listConfigs());
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody AiModelConfigSaveRequest request) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(Map.of("configId", aiModelConfigService.createConfig(request)));
    }

    @PutMapping("/{configId}")
    public ApiResponse<Void> update(@PathVariable Long configId, @Valid @RequestBody AiModelConfigSaveRequest request) {
        StpUtil.checkRole(RoleCode.ADMIN);
        aiModelConfigService.updateConfig(configId, request);
        return ApiResponse.success("模型配置已更新", null);
    }

    @PostMapping("/{configId}/enable")
    public ApiResponse<Void> enable(@PathVariable Long configId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        aiModelConfigService.enableConfig(configId);
        return ApiResponse.success("模型已启用", null);
    }

    @PostMapping("/{configId}/disable")
    public ApiResponse<Void> disable(@PathVariable Long configId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        aiModelConfigService.disableConfig(configId);
        return ApiResponse.success("模型已停用", null);
    }

    @DeleteMapping("/{configId}")
    public ApiResponse<Void> delete(@PathVariable Long configId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        aiModelConfigService.deleteConfig(configId);
        return ApiResponse.success("模型配置已删除", null);
    }

    @PostMapping("/{configId}/test")
    public ApiResponse<AiModelTestResultVo> test(@PathVariable Long configId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(aiModelConfigService.testConfig(configId));
    }
}
