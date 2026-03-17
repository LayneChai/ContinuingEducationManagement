package com.qizhi.continueeducation.module.certificate.service;

import com.qizhi.continueeducation.module.certificate.dto.CertificateApplyRequest;
import com.qizhi.continueeducation.module.certificate.dto.CertificateReviewRequest;
import com.qizhi.continueeducation.module.certificate.vo.CertificateApplyVo;

import java.util.List;

public interface CertificateService {

    void apply(Long studentId, Long courseId, CertificateApplyRequest request);

    List<CertificateApplyVo> studentApplications(Long studentId);

    List<CertificateApplyVo> adminApplications(Integer status);

    List<CertificateApplyVo> teacherApplications(Long teacherId, Integer status);

    void review(Long adminId, Long applyId, CertificateReviewRequest request);

    void teacherReview(Long teacherId, Long applyId, CertificateReviewRequest request);
}
