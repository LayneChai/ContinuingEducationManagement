package com.qizhi.continueeducation.module.assignment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qizhi.continueeducation.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_assignment_submission")
public class EduAssignmentSubmission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long assignmentId;
    private Long courseId;
    private Long studentId;
    private String content;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Integer submitCount;
    private Integer status;
    private LocalDateTime submittedTime;
}
