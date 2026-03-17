package com.qizhi.continueeducation.module.course.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourseLessonVo {
    private Long id;
    private Long chapterId;
    private String title;
    private Integer lessonType;
    private String resourceUrl;
    private String content;
    private Integer durationSeconds;
    private Integer previewEnabled;
    private Integer sort;
    private Integer status;
    private BigDecimal learningProgress;
    private Boolean completed;
}
