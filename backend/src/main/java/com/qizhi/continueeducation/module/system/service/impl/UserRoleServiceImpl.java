package com.qizhi.continueeducation.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qizhi.continueeducation.module.system.entity.SysRole;
import com.qizhi.continueeducation.module.system.entity.SysUserRole;
import com.qizhi.continueeducation.module.system.mapper.SysRoleMapper;
import com.qizhi.continueeducation.module.system.mapper.SysUserRoleMapper;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        return getRoleCodesByUserIds(Collections.singletonList(userId)).getOrDefault(userId, Collections.emptyList());
    }

    @Override
    public Map<Long, List<String>> getRoleCodesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Collections.emptyMap();
        }

        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, SysRole> roleMap = sysRoleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<String>> result = new LinkedHashMap<>();
        for (SysUserRole userRole : userRoles) {
            SysRole role = roleMap.get(userRole.getRoleId());
            if (role == null) {
                continue;
            }
            result.computeIfAbsent(userRole.getUserId(), key -> new ArrayList<>()).add(role.getRoleCode());
        }
        return result;
    }

    @Override
    public List<Long> getUserIdsByRoleCode(String roleCode) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getStatus, 1)
                .last("limit 1"));
        if (role == null) {
            return Collections.emptyList();
        }
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, role.getId()))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
    }

    @Override
    public List<SysRole> listActiveRoles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRoles(Long userId, List<String> roleCodes) {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, roleCodes)
                .eq(SysRole::getStatus, 1));
        if (roles.size() != roleCodes.size()) {
            throw new IllegalArgumentException("存在无效角色");
        }

        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        for (SysRole role : roles) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            sysUserRoleMapper.insert(userRole);
        }
    }
}
