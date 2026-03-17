package com.qizhi.continueeducation.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;
    private String email;
    private String studentNo;
    private String className;
    private String title;
    private String bio;
    private String password;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
