package com.qizhi.continueeducation.module.system.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVo {

    private String roleCode;
    private String title;
    private List<DashboardCardVo> cards;
    private Map<String, Object> extra;
}
