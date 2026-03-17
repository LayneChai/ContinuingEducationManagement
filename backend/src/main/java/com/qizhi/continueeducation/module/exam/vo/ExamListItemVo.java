package com.qizhi.continueeducation.module.exam.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamListItemVo {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private Integer durationMinutes;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer attemptLimit;
    private Integer questionCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private Integer myAttemptCount;
    private BigDecimal latestScore;
    private Integer latestPass;
}
