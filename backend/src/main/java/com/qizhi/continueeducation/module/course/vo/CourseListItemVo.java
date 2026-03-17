package com.qizhi.continueeducation.module.course.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseListItemVo {
    private Long id;
    private String courseCode;
    private String title;
    private String subtitle;
    private String teacherName;
    private Long teacherId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal requiredHours;
    private Integer totalLessons;
    private Integer auditStatus;
    private Integer status;
    private String auditRemark;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private Boolean enrolled;
    private Integer enrollmentStatus;
    private BigDecimal learningProgress;
}
