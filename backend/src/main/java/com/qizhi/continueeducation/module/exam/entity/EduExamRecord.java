package com.qizhi.continueeducation.module.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qizhi.continueeducation.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_exam_record")
public class EduExamRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long courseId;
    private Long studentId;
    private Integer attemptNo;
    private Integer status;
    private BigDecimal objectiveScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private Integer isPass;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
}
