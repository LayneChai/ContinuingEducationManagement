package com.qizhi.continueeducation.module.assignment.dto;

import lombok.Data;

@Data
public class AssignmentSubmitRequest {
    private String content;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
}
