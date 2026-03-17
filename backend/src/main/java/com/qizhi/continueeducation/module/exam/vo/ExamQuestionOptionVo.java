package com.qizhi.continueeducation.module.exam.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamQuestionOptionVo {
    private Long id;
    private String optionLabel;
    private String optionContent;
    private Integer sort;
}
