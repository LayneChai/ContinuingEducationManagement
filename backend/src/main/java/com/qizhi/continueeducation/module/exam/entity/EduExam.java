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
@TableName("edu_exam")
public class EduExam extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer durationMinutes;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer attemptLimit;
    private Integer questionCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
