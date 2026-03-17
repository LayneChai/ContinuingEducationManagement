package com.qizhi.continueeducation.module.certificate.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateApplyVo {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private String applyReason;
    private Integer status;
    private String reviewRemark;
    private LocalDateTime applyTime;
    private LocalDateTime reviewTime;
    private CertificateRecordVo record;
}
