package com.qizhi.continueeducation.module.system.service;

import com.qizhi.continueeducation.module.system.entity.SysRole;

import java.util.List;
import java.util.Map;

public interface UserRoleService {

    List<String> getRoleCodesByUserId(Long userId);

    Map<Long, List<String>> getRoleCodesByUserIds(List<Long> userIds);

    List<Long> getUserIdsByRoleCode(String roleCode);

    List<SysRole> listActiveRoles();

    void bindRoles(Long userId, List<String> roleCodes);
}
