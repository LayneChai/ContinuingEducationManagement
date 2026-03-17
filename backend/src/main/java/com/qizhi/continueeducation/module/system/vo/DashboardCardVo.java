package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardCardVo {

    private String key;
    private String title;
    private Long value;
    private String unit;
}
