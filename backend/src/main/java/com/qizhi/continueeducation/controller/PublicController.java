package com.qizhi.continueeducation.controller;

import com.qizhi.continueeducation.common.domain.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("systemName", "启智继续教育管理系统");
        data.put("status", "UP");
        data.put("time", LocalDateTime.now());
        return ApiResponse.success(data);
    }
}
