package com.qizhi.continueeducation.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChapterSaveRequest {

    @NotBlank(message = "章节标题不能为空")
    private String title;
    private Integer sort;
    private String description;
}
