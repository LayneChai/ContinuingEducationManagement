package com.qizhi.continueeducation.module.course.entity;

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
@TableName("edu_course")
public class EduCourse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseCode;
    private String title;
    private String subtitle;
    private String coverUrl;
    private Long teacherId;
    private Long categoryId;
    private String description;
    private String targetUser;
    private BigDecimal requiredHours;
    private Integer totalLessons;
    private Integer examRequired;
    private Integer assignmentRequired;
    private Integer certificateEnabled;
    private Integer auditStatus;
    private Integer status;
    private String auditRemark;
    private LocalDateTime publishTime;
}
