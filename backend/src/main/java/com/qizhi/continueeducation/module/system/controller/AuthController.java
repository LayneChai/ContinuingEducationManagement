package com.qizhi.continueeducation.module.system.controller;

import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.system.dto.LoginRequest;
import com.qizhi.continueeducation.module.system.service.AuthService;
import com.qizhi.continueeducation.module.system.vo.CurrentUserVo;
import com.qizhi.continueeducation.module.system.vo.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserVo> currentUser() {
        return ApiResponse.success(authService.currentUser());
    }
}
