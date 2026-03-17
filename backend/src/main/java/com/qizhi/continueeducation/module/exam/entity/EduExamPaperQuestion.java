package com.qizhi.continueeducation.module.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("edu_exam_paper_question")
public class EduExamPaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long questionId;
    private BigDecimal score;
    private Integer sort;
}
