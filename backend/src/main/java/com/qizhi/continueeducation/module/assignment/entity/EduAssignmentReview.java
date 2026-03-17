package com.qizhi.continueeducation.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("edu_assignment_review")
public class EduAssignmentReview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private Long reviewerId;
    private BigDecimal score;
    private String comment;
    private String aiComment;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}
