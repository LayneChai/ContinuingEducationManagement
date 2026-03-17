package com.qizhi.continueeducation.module.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AssignmentSaveRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotBlank(message = "作业标题不能为空")
    private String title;
    private String description;
    private BigDecimal totalScore;
    private Integer allowResubmit;
    private LocalDateTime deadline;
}
