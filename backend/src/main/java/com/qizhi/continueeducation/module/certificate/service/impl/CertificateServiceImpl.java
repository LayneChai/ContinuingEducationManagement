package com.qizhi.continueeducation.module.certificate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.certificate.dto.CertificateApplyRequest;
import com.qizhi.continueeducation.module.certificate.dto.CertificateReviewRequest;
import com.qizhi.continueeducation.module.certificate.entity.EduCertificateApply;
import com.qizhi.continueeducation.module.certificate.entity.EduCertificateRecord;
import com.qizhi.continueeducation.module.certificate.entity.EduCertificateTemplate;
import com.qizhi.continueeducation.module.certificate.mapper.EduCertificateApplyMapper;
import com.qizhi.continueeducation.module.certificate.mapper.EduCertificateRecordMapper;
import com.qizhi.continueeducation.module.certificate.mapper.EduCertificateTemplateMapper;
import com.qizhi.continueeducation.module.certificate.service.CertificateService;
import com.qizhi.continueeducation.module.certificate.vo.CertificateApplyVo;
import com.qizhi.continueeducation.module.certificate.vo.CertificateRecordVo;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.learning.entity.EduCourseHourStat;
import com.qizhi.continueeducation.module.learning.mapper.EduCourseHourStatMapper;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final EduCertificateApplyMapper applyMapper;
    private final EduCertificateRecordMapper recordMapper;
    private final EduCertificateTemplateMapper templateMapper;
    private final EduCourseMapper courseMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final EduCourseHourStatMapper hourStatMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void apply(Long studentId, Long courseId, CertificateApplyRequest request) {
        EduCourse course = requireCourse(courseId);
        if (!Integer.valueOf(1).equals(course.getCertificateEnabled())) {
            throw new IllegalStateException("该课程未开启证书申请");
        }
        EduCourseEnrollment enrollment = enrollmentMapper.selectOne(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getCourseId, courseId)
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .last("limit 1"));
        if (enrollment == null || !Integer.valueOf(2).equals(enrollment.getStatus())) {
            throw new IllegalStateException("课程尚未完成，暂不能申请证书");
        }
        EduCourseHourStat stat = hourStatMapper.selectOne(new LambdaQueryWrapper<EduCourseHourStat>()
                .eq(EduCourseHourStat::getCourseId, courseId)
                .eq(EduCourseHourStat::getStudentId, studentId)
                .last("limit 1"));
        if (stat == null || !Integer.valueOf(1).equals(stat.getQualified())) {
            throw new IllegalStateException("学时未达标，暂不能申请证书");
        }

        EduCertificateApply existed = applyMapper.selectOne(new LambdaQueryWrapper<EduCertificateApply>()
                .eq(EduCertificateApply::getCourseId, courseId)
                .eq(EduCertificateApply::getStudentId, studentId)
                .last("limit 1"));
        if (existed != null) {
            if (Integer.valueOf(2).equals(existed.getStatus())) {
                existed.setStatus(0);
                existed.setApplyReason(request == null ? null : request.getApplyReason());
                existed.setReviewRemark(null);
                existed.setReviewerId(null);
                existed.setReviewTime(null);
                existed.setApplyTime(LocalDateTime.now());
                applyMapper.updateById(existed);
                return;
            }
            throw new IllegalStateException("该课程已提交过证书申请");
        }

        EduCertificateApply apply = new EduCertificateApply();
        apply.setCourseId(courseId);
        apply.setStudentId(studentId);
        apply.setTemplateId(defaultTemplateId());
        apply.setApplyReason(request == null ? null : request.getApplyReason());
        apply.setStatus(0);
        apply.setApplyTime(LocalDateTime.now());
        applyMapper.insert(apply);
    }

    @Override
    public List<CertificateApplyVo> studentApplications(Long studentId) {
        List<EduCertificateApply> applies = applyMapper.selectList(new LambdaQueryWrapper<EduCertificateApply>()
                .eq(EduCertificateApply::getStudentId, studentId)
                .orderByDesc(EduCertificateApply::getApplyTime));
        return toApplyVos(applies);
    }

    @Override
    public List<CertificateApplyVo> adminApplications(Integer status) {
        List<EduCertificateApply> applies = applyMapper.selectList(new LambdaQueryWrapper<EduCertificateApply>()
                .eq(status != null, EduCertificateApply::getStatus, status)
                .orderByAsc(EduCertificateApply::getStatus)
                .orderByDesc(EduCertificateApply::getApplyTime));
        return toApplyVos(applies);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long adminId, Long applyId, CertificateReviewRequest request) {
        EduCertificateApply apply = requireApply(applyId);
        if (!Integer.valueOf(0).equals(apply.getStatus()) && !Integer.valueOf(2).equals(request.getStatus())) {
            throw new IllegalStateException("当前申请状态不可审核通过");
        }
        apply.setStatus(request.getStatus());
        apply.setReviewerId(adminId);
        apply.setReviewRemark(request.getReviewRemark());
        apply.setReviewTime(LocalDateTime.now());
        applyMapper.updateById(apply);

        if (Integer.valueOf(1).equals(request.getStatus())) {
            EduCertificateRecord record = recordMapper.selectOne(new LambdaQueryWrapper<EduCertificateRecord>()
                    .eq(EduCertificateRecord::getApplyId, applyId)
                    .last("limit 1"));
            if (record == null) {
                record = new EduCertificateRecord();
                record.setApplyId(applyId);
                record.setCertificateNo(generateCertificateNo(apply.getCourseId(), apply.getStudentId()));
            }
            EduCourse course = requireCourse(apply.getCourseId());
            record.setCertificateTitle((request.getCertificateTitle() == null || request.getCertificateTitle().isBlank())
                    ? course.getTitle() + "结业证书"
                    : request.getCertificateTitle());
            record.setCertificateUrl(request.getCertificateUrl());
            record.setIssueTime(LocalDateTime.now());
            record.setStatus(1);
            if (record.getId() == null) {
                recordMapper.insert(record);
            } else {
                recordMapper.updateById(record);
            }
        }
    }

    private List<CertificateApplyVo> toApplyVos(List<EduCertificateApply> applies) {
        if (applies.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduCourse> courseMap = courseMapper.selectBatchIds(applies.stream().map(EduCertificateApply::getCourseId).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourse::getId, Function.identity(), (a, b) -> a));
        Map<Long, SysUser> studentMap = userMapper.selectBatchIds(applies.stream().map(EduCertificateApply::getStudentId).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        Map<Long, EduCertificateRecord> recordMap = recordMapper.selectList(new LambdaQueryWrapper<EduCertificateRecord>()
                        .in(EduCertificateRecord::getApplyId, applies.stream().map(EduCertificateApply::getId).toList()))
                .stream().collect(Collectors.toMap(EduCertificateRecord::getApplyId, Function.identity(), (a, b) -> a));

        return applies.stream().map(item -> {
            EduCourse course = courseMap.get(item.getCourseId());
            SysUser student = studentMap.get(item.getStudentId());
            EduCertificateRecord record = recordMap.get(item.getId());
            return CertificateApplyVo.builder()
                    .id(item.getId())
                    .courseId(item.getCourseId())
                    .courseTitle(course == null ? null : course.getTitle())
                    .studentId(item.getStudentId())
                    .studentName(student == null ? null : student.getRealName())
                    .applyReason(item.getApplyReason())
                    .status(item.getStatus())
                    .reviewRemark(item.getReviewRemark())
                    .applyTime(item.getApplyTime())
                    .reviewTime(item.getReviewTime())
                    .record(record == null ? null : CertificateRecordVo.builder()
                            .recordId(record.getId())
                            .certificateNo(record.getCertificateNo())
                            .certificateTitle(record.getCertificateTitle())
                            .certificateUrl(record.getCertificateUrl())
                            .issueTime(record.getIssueTime())
                            .status(record.getStatus())
                            .build())
                    .build();
        }).toList();
    }

    private Long defaultTemplateId() {
        EduCertificateTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<EduCertificateTemplate>()
                .eq(EduCertificateTemplate::getStatus, 1)
                .last("limit 1"));
        return template == null ? null : template.getId();
    }

    private String generateCertificateNo(Long courseId, Long studentId) {
        return "CERT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + courseId + "-" + studentId;
    }

    private EduCourse requireCourse(Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        return course;
    }

    private EduCertificateApply requireApply(Long applyId) {
        EduCertificateApply apply = applyMapper.selectById(applyId);
        if (apply == null) {
            throw new IllegalArgumentException("证书申请不存在");
        }
        return apply;
    }
}
