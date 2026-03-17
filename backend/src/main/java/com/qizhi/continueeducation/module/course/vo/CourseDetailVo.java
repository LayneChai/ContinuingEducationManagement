package com.qizhi.continueeducation.module.course.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CourseDetailVo {
    private Long id;
    private String courseCode;
    private String title;
    private String subtitle;
    private String coverUrl;
    private Long teacherId;
    private String teacherName;
    private Long categoryId;
    private String categoryName;
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
    private Boolean enrolled;
    private Integer enrollmentStatus;
    private BigDecimal learningProgress;
    private BigDecimal completedHours;
    private Integer qualified;
    private Integer completedLessons;
    private List<CourseChapterVo> chapters;
}
