package com.qizhi.continueeducation.module.learning.entity;

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
@TableName("edu_learning_record")
public class EduLearningRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long lessonId;
    private Long studentId;
    private Integer studySeconds;
    private BigDecimal progressPercent;
    private Integer lastPosition;
    private Integer isCompleted;
    private LocalDateTime firstStudyTime;
    private LocalDateTime lastStudyTime;
}
