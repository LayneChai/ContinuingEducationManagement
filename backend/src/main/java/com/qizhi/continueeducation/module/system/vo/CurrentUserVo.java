package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrentUserVo {

    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private List<String> roles;
}
