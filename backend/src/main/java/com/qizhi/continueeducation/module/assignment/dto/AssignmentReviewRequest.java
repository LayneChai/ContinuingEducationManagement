package com.qizhi.continueeducation.module.assignment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssignmentReviewRequest {
    @NotNull(message = "评分不能为空")
    private BigDecimal score;
    private String comment;
    private String aiComment;
}
