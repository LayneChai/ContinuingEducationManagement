package com.qizhi.continueeducation.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.system.entity.SysRole;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysRoleMapper;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemBootstrapService implements CommandLineRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long roleCount = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>());
        initDefaultAdmin();
        log.info("System bootstrap finished, role count: {}", roleCount);
    }

    private void initDefaultAdmin() {
        SysUser admin = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, "admin")
                .eq(SysUser::getDeleted, 0)
                .last("limit 1"));
        if (admin != null) {
            return;
        }

        SysUser user = new SysUser();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("Admin@123456"));
        user.setRealName("系统管理员");
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);
        userRoleService.bindRoles(user.getId(), Collections.singletonList("ADMIN"));
        log.info("Initialized default admin account: username=admin, password=Admin@123456");
    }
}
