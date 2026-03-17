package com.qizhi.continueeducation.module.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamQuestionSaveRequest {

    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    @NotNull(message = "题型不能为空")
    private Integer questionType;
    @NotBlank(message = "题干不能为空")
    private String stem;
    private String analysis;
    private Integer difficulty;
    @NotNull(message = "分值不能为空")
    private BigDecimal score;
    private String correctAnswer;
    private Integer status;
    @Valid
    private List<ExamQuestionOptionRequest> options;
}
