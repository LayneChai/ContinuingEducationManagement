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
@TableName("edu_exam_question")
public class EduExamQuestion extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Integer questionType;
    private String stem;
    private String analysis;
    private Integer difficulty;
    private BigDecimal score;
    private String correctAnswer;
    private Integer status;
}
