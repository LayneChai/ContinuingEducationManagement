package com.qizhi.continueeducation.module.learning.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LearningOverviewVo {

    private Long courseId;
    private Long studentId;
    private BigDecimal requiredHours;
    private BigDecimal completedHours;
    private BigDecimal completionRate;
    private Integer qualified;
    private Integer totalLessons;
    private Integer completedLessons;
}
