package com.qizhi.continueeducation.module.course.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qizhi.continueeducation.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("edu_course_lesson")
public class EduCourseLesson extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long chapterId;
    private String title;
    private Integer lessonType;
    private String resourceUrl;
    private String content;
    private Integer durationSeconds;
    private Integer previewEnabled;
    private Integer sort;
    private Integer status;
}
