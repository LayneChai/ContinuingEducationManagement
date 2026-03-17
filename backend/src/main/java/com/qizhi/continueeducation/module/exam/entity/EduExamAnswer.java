package com.qizhi.continueeducation.module.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qizhi.continueeducation.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_answer")
public class EduExamAnswer extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long examId;
    private Long questionId;
    private Long studentId;
    private String studentAnswer;
    private Integer isCorrect;
    private BigDecimal score;
    private BigDecimal aiScore;
    private String aiComment;
    private String reviewComment;
}
