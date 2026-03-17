package com.qizhi.continueeducation.module.certificate.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CertificateRecordVo {
    private Long recordId;
    private String certificateNo;
    private String certificateTitle;
    private String certificateUrl;
    private LocalDateTime issueTime;
    private Integer status;
}
