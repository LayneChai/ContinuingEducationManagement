package com.qizhi.continueeducation.module.course.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CourseChapterVo {
    private Long id;
    private Long courseId;
    private String title;
    private Integer sort;
    private String description;
    private List<CourseLessonVo> lessons;
}
