package com.qizhi.continueeducation.module.system.service;

import com.qizhi.continueeducation.module.system.dto.CreateUserRequest;
import com.qizhi.continueeducation.module.system.vo.RoleOptionVo;
import com.qizhi.continueeducation.module.system.vo.UserListItemVo;

import java.util.List;

public interface UserAdminService {

    List<UserListItemVo> listUsers(String keyword, String roleCode, Integer status);

    List<RoleOptionVo> listRoles();

    Long createUser(CreateUserRequest request);
}
