package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserListItemVo {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer status;
    private String studentNo;
    private String className;
    private String title;
    private String bio;
    private List<String> roles;
    private LocalDateTime createTime;
}
