package com.qizhi.continueeducation.module.exam.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ExamQuestionVo {
    private Long id;
    private Long courseId;
    private Integer questionType;
    private String stem;
    private String analysis;
    private Integer difficulty;
    private BigDecimal score;
    private String correctAnswer;
    private Integer status;
    private List<ExamQuestionOptionVo> options;
}
