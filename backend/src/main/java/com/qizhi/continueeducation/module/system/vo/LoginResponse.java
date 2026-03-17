package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {

    private Long userId;
    private String username;
    private String realName;
    private String token;
    private List<String> roles;
}
