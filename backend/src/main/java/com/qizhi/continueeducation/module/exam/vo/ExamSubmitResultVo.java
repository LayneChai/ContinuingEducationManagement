package com.qizhi.continueeducation.module.exam.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ExamSubmitResultVo {
    private Long recordId;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private Integer passed;
    private Integer pendingReviewCount;
}
