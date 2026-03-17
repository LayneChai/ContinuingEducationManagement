package com.qizhi.continueeducation.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonSaveRequest {

    @NotNull(message = "章节ID不能为空")
    private Long chapterId;
    @NotBlank(message = "课时标题不能为空")
    private String title;
    @NotNull(message = "课时类型不能为空")
    private Integer lessonType;
    private String resourceUrl;
    private String content;
    private Integer durationSeconds;
    private Integer previewEnabled;
    private Integer sort;
    private Integer status;
}
