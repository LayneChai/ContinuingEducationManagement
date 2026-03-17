package com.qizhi.continueeducation.module.exam.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExamSubmitRequest {

    @Valid
    @NotEmpty(message = "答题数据不能为空")
    private List<ExamSubmitAnswerRequest> answers;
}
