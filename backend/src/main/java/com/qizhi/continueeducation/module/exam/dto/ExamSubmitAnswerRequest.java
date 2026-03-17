package com.qizhi.continueeducation.module.exam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExamSubmitAnswerRequest {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;
    private String answer;
}
