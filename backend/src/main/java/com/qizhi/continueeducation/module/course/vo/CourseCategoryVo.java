package com.qizhi.continueeducation.module.course.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseCategoryVo {
    private Long id;
    private Long parentId;
    private String name;
    private String code;
}
