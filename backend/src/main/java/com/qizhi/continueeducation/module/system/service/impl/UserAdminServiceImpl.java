package com.qizhi.continueeducation.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.system.dto.CreateUserRequest;
import com.qizhi.continueeducation.module.system.entity.SysRole;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import com.qizhi.continueeducation.module.system.service.UserAdminService;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import com.qizhi.continueeducation.module.system.vo.RoleOptionVo;
import com.qizhi.continueeducation.module.system.vo.UserListItemVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserListItemVo> listUsers(String keyword, String roleCode, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDeleted, 0)
                .orderByDesc(SysUser::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        List<SysUser> users = sysUserMapper.selectList(wrapper);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<String>> roleMap = userRoleService.getRoleCodesByUserIds(
                users.stream().map(SysUser::getId).toList()
        );

        return users.stream()
                .filter(user -> {
                    if (!StringUtils.hasText(roleCode)) {
                        return true;
                    }
                    return roleMap.getOrDefault(user.getId(), Collections.emptyList()).contains(roleCode);
                })
                .map(user -> UserListItemVo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .status(user.getStatus())
                        .studentNo(user.getStudentNo())
                        .title(user.getTitle())
                        .roles(roleMap.getOrDefault(user.getId(), Collections.emptyList()))
                        .createTime(user.getCreateTime())
                        .build())
                .toList();
    }

    @Override
    public List<RoleOptionVo> listRoles() {
        return userRoleService.listActiveRoles().stream()
                .map(role -> RoleOptionVo.builder()
                        .id(role.getId())
                        .roleCode(role.getRoleCode())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(CreateUserRequest request) {
        long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStudentNo(request.getStudentNo());
        user.setTitle(request.getTitle());
        user.setBio(request.getBio());
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        userRoleService.bindRoles(user.getId(), request.getRoleCodes());
        return user.getId();
    }
}
