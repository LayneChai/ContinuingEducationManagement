package com.qizhi.continueeducation.module.exam.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExamQuestionOptionRequest {

    @NotBlank(message = "选项标识不能为空")
    private String optionLabel;
    @NotBlank(message = "选项内容不能为空")
    private String optionContent;
    private Integer isCorrect;
    private Integer sort;
}
