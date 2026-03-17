package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleOptionVo {

    private Long id;
    private String roleCode;
    private String roleName;
}
