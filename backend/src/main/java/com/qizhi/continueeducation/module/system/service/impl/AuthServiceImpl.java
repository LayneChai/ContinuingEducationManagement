package com.qizhi.continueeducation.module.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.system.dto.LoginRequest;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import com.qizhi.continueeducation.module.system.service.AuthService;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import com.qizhi.continueeducation.module.system.vo.CurrentUserVo;
import com.qizhi.continueeducation.module.system.vo.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername())
                .eq(SysUser::getDeleted, 0)
                .last("limit 1"));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (Integer.valueOf(1).equals(user.getStatus()) == false) {
            throw new IllegalStateException("账号已被禁用");
        }
        List<String> roles = userRoleService.getRoleCodesByUserId(user.getId());
        StpUtil.login(user.getId());
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .token(StpUtil.getTokenValue())
                .roles(roles)
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public CurrentUserVo currentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalStateException("当前用户不存在");
        }
        return CurrentUserVo.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .status(user.getStatus())
                .roles(userRoleService.getRoleCodesByUserId(userId))
                .build();
    }
}
