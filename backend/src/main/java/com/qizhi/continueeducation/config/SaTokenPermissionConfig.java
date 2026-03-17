package com.qizhi.continueeducation.config;

import cn.dev33.satoken.stp.StpInterface;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import com.qizhi.continueeducation.module.system.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SaTokenPermissionConfig implements StpInterface {

    private final UserRoleService userRoleService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roles = getRoleList(loginId, loginType);
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        permissions.add("portal:view");
        if (roles.contains(RoleCode.ADMIN)) {
            permissions.add("dashboard:admin");
            permissions.add("user:manage");
            permissions.add("teacher:manage");
            permissions.add("student:manage");
            permissions.add("course:audit");
            permissions.add("certificate:audit");
        }
        if (roles.contains(RoleCode.TEACHER)) {
            permissions.add("dashboard:teacher");
            permissions.add("course:manage");
            permissions.add("exam:manage");
            permissions.add("assignment:manage");
            permissions.add("certificate:audit");
            permissions.add("ai:review");
        }
        if (roles.contains(RoleCode.STUDENT)) {
            permissions.add("dashboard:student");
            permissions.add("course:learn");
            permissions.add("exam:take");
            permissions.add("assignment:submit");
            permissions.add("certificate:apply");
            permissions.add("ai:chat");
        }
        return new ArrayList<>(permissions);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userRoleService.getRoleCodesByUserId(Long.parseLong(String.valueOf(loginId)));
    }
}
