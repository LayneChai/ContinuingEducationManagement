package com.qizhi.continueeducation.module.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamSaveRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotBlank(message = "考试名称不能为空")
    private String title;
    private String description;
    private Integer durationMinutes;
    private BigDecimal passScore;
    private Integer attemptLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @NotEmpty(message = "请选择考试题目")
    private List<Long> questionIds;
}
