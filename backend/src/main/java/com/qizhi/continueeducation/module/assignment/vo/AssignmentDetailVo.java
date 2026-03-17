package com.qizhi.continueeducation.module.assignment.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentDetailVo {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private BigDecimal totalScore;
    private Integer allowResubmit;
    private LocalDateTime deadline;
    private Integer status;
    private AssignmentSubmissionVo submission;
}
