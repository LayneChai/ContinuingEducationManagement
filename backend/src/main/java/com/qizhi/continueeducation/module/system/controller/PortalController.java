package com.qizhi.continueeducation.module.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.system.service.PortalService;
import com.qizhi.continueeducation.module.system.vo.DashboardVo;
import com.qizhi.continueeducation.module.system.vo.MenuItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;

    @GetMapping("/menus")
    public ApiResponse<List<MenuItemVo>> menus() {
        StpUtil.checkLogin();
        return ApiResponse.success(portalService.getCurrentUserMenus());
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardVo> dashboard() {
        StpUtil.checkLogin();
        return ApiResponse.success(portalService.getCurrentUserDashboard());
    }
}
