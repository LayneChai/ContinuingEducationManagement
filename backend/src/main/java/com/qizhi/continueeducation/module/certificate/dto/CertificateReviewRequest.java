package com.qizhi.continueeducation.module.certificate.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CertificateReviewRequest {
    @NotNull(message = "审核结果不能为空")
    @Min(value = 1, message = "审核结果不合法")
    @Max(value = 2, message = "审核结果不合法")
    private Integer status;
    private String reviewRemark;
    private String certificateTitle;
    private String certificateUrl;
}
