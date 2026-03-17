package com.qizhi.continueeducation.module.certificate.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.certificate.dto.CertificateReviewRequest;
import com.qizhi.continueeducation.module.certificate.service.CertificateService;
import com.qizhi.continueeducation.module.certificate.vo.CertificateApplyVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/certificates")
public class AdminCertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public ApiResponse<List<CertificateApplyVo>> list(@RequestParam(required = false) Integer status) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(certificateService.adminApplications(status));
    }

    @PostMapping("/{applyId}/review")
    public ApiResponse<Void> review(@PathVariable Long applyId, @Valid @RequestBody CertificateReviewRequest request) {
        StpUtil.checkRole(RoleCode.ADMIN);
        certificateService.review(StpUtil.getLoginIdAsLong(), applyId, request);
        return ApiResponse.success("证书审核完成", null);
    }
}
