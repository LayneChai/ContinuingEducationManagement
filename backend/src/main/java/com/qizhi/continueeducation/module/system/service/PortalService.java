package com.qizhi.continueeducation.module.system.service;

import com.qizhi.continueeducation.module.system.vo.DashboardVo;
import com.qizhi.continueeducation.module.system.vo.MenuItemVo;

import java.util.List;

public interface PortalService {

    List<MenuItemVo> getCurrentUserMenus();

    DashboardVo getCurrentUserDashboard();
}
