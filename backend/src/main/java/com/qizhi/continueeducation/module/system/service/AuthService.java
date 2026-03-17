package com.qizhi.continueeducation.module.system.service;

import com.qizhi.continueeducation.module.system.dto.LoginRequest;
import com.qizhi.continueeducation.module.system.vo.CurrentUserVo;
import com.qizhi.continueeducation.module.system.vo.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    CurrentUserVo currentUser();
}
