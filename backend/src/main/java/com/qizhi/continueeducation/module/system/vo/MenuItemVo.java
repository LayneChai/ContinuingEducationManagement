package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MenuItemVo {

    private String name;
    private String path;
    private String component;
    private String title;
    private String icon;
    private String permission;
    private List<MenuItemVo> children;
}
