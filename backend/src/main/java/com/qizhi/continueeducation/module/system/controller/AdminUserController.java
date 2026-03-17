package com.qizhi.continueeducation.module.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.system.dto.CreateUserRequest;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import com.qizhi.continueeducation.module.system.service.UserAdminService;
import com.qizhi.continueeducation.module.system.vo.RoleOptionVo;
import com.qizhi.continueeducation.module.system.vo.UserListItemVo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminUserController {

    private final UserAdminService userAdminService;

    @GetMapping("/users")
    public ApiResponse<List<UserListItemVo>> listUsers(@RequestParam(required = false) String keyword,
                                                       @RequestParam(required = false) String roleCode,
                                                       @RequestParam(required = false) Integer status) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(userAdminService.listUsers(keyword, roleCode, status));
    }

    @PostMapping("/users")
    public ApiResponse<Map<String, Long>> createUser(@Valid @RequestBody CreateUserRequest request) {
        StpUtil.checkRole(RoleCode.ADMIN);
        Long userId = userAdminService.createUser(request);
        return ApiResponse.success(Map.of("userId", userId));
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleOptionVo>> listRoles() {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(userAdminService.listRoles());
    }
}
