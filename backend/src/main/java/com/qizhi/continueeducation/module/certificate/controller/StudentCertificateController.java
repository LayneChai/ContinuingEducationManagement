package com.qizhi.continueeducation.module.certificate.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.certificate.dto.CertificateApplyRequest;
import com.qizhi.continueeducation.module.certificate.service.CertificateService;
import com.qizhi.continueeducation.module.certificate.vo.CertificateApplyVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/certificates")
public class StudentCertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public ApiResponse<List<CertificateApplyVo>> list() {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(certificateService.studentApplications(StpUtil.getLoginIdAsLong()));
    }

    @PostMapping("/courses/{courseId}/apply")
    public ApiResponse<Void> apply(@PathVariable Long courseId, @RequestBody(required = false) CertificateApplyRequest request) {
        StpUtil.checkRole(RoleCode.STUDENT);
        certificateService.apply(StpUtil.getLoginIdAsLong(), courseId, request);
        return ApiResponse.success("证书申请已提交", null);
    }
}
