package com.qizhi.continueeducation.module.assignment.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentSubmissionVo {
    private Long submissionId;
    private Long studentId;
    private String studentName;
    private String content;
    private String fileName;
    private String fileUrl;
    private Integer submitCount;
    private Integer status;
    private LocalDateTime submittedTime;
    private BigDecimal score;
    private String comment;
    private String aiComment;
    private LocalDateTime reviewTime;
}
