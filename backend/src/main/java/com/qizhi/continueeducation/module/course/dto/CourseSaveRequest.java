package com.qizhi.continueeducation.module.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSaveRequest {

    @NotBlank(message = "课程标题不能为空")
    private String title;
    private String subtitle;
    private String coverUrl;
    private Long categoryId;
    private String description;
    private String targetUser;
    @NotNull(message = "要求学时不能为空")
    private BigDecimal requiredHours;
    private Integer examRequired;
    private Integer assignmentRequired;
    private Integer certificateEnabled;
}
