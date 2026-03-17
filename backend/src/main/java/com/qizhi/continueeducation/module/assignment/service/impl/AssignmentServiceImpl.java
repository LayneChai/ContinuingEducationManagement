package com.qizhi.continueeducation.module.assignment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentReviewRequest;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSaveRequest;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSubmitRequest;
import com.qizhi.continueeducation.module.assignment.entity.EduAssignment;
import com.qizhi.continueeducation.module.assignment.entity.EduAssignmentReview;
import com.qizhi.continueeducation.module.assignment.entity.EduAssignmentSubmission;
import com.qizhi.continueeducation.module.assignment.mapper.EduAssignmentMapper;
import com.qizhi.continueeducation.module.assignment.mapper.EduAssignmentReviewMapper;
import com.qizhi.continueeducation.module.assignment.mapper.EduAssignmentSubmissionMapper;
import com.qizhi.continueeducation.module.assignment.service.AssignmentService;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentDetailVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentListItemVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentSubmissionVo;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final EduAssignmentMapper assignmentMapper;
    private final EduAssignmentSubmissionMapper submissionMapper;
    private final EduAssignmentReviewMapper reviewMapper;
    private final EduCourseMapper courseMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<AssignmentListItemVo> teacherAssignments(Long teacherId, Long courseId) {
        List<Long> courseIds = teacherCourseIds(teacherId, courseId);
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<EduAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<EduAssignment>()
                .in(EduAssignment::getCourseId, courseIds)
                .orderByDesc(EduAssignment::getCreateTime));
        return toAssignmentList(assignments, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssignment(Long teacherId, AssignmentSaveRequest request) {
        requireTeacherCourse(teacherId, request.getCourseId());
        EduAssignment assignment = new EduAssignment();
        fillAssignment(assignment, request, teacherId);
        assignment.setStatus(0);
        assignmentMapper.insert(assignment);
        return assignment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAssignment(Long teacherId, Long assignmentId, AssignmentSaveRequest request) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireTeacherCourse(teacherId, assignment.getCourseId());
        if (!assignment.getCourseId().equals(request.getCourseId())) {
            throw new IllegalArgumentException("不允许修改作业所属课程");
        }
        fillAssignment(assignment, request, teacherId);
        assignment.setStatus(0);
        assignmentMapper.updateById(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAssignment(Long teacherId, Long assignmentId) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireTeacherCourse(teacherId, assignment.getCourseId());
        assignment.setStatus(1);
        assignmentMapper.updateById(assignment);
    }

    @Override
    public AssignmentDetailVo teacherAssignmentDetail(Long teacherId, Long assignmentId) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireTeacherCourse(teacherId, assignment.getCourseId());
        return toAssignmentDetail(assignment, null);
    }

    @Override
    public List<AssignmentSubmissionVo> teacherSubmissions(Long teacherId, Long assignmentId) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireTeacherCourse(teacherId, assignment.getCourseId());
        List<EduAssignmentSubmission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<EduAssignmentSubmission>()
                .eq(EduAssignmentSubmission::getAssignmentId, assignmentId)
                .orderByDesc(EduAssignmentSubmission::getSubmittedTime));
        return toSubmissionVos(submissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewSubmission(Long teacherId, Long submissionId, AssignmentReviewRequest request) {
        EduAssignmentSubmission submission = requireSubmission(submissionId);
        EduAssignment assignment = requireAssignment(submission.getAssignmentId());
        requireTeacherCourse(teacherId, assignment.getCourseId());

        EduAssignmentReview review = reviewMapper.selectOne(new LambdaQueryWrapper<EduAssignmentReview>()
                .eq(EduAssignmentReview::getSubmissionId, submissionId)
                .last("limit 1"));
        if (review == null) {
            review = new EduAssignmentReview();
            review.setSubmissionId(submissionId);
            review.setReviewerId(teacherId);
            review.setCreateTime(LocalDateTime.now());
        }
        review.setScore(request.getScore());
        review.setComment(request.getComment());
        review.setAiComment(request.getAiComment());
        review.setReviewTime(LocalDateTime.now());
        if (review.getId() == null) {
            reviewMapper.insert(review);
        } else {
            reviewMapper.updateById(review);
        }

        submission.setStatus(2);
        submissionMapper.updateById(submission);
    }

    @Override
    public List<AssignmentListItemVo> studentAssignments(Long studentId, Long courseId) {
        List<Long> courseIds = enrolledCourseIds(studentId, courseId);
        if (courseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<EduAssignment> assignments = assignmentMapper.selectList(new LambdaQueryWrapper<EduAssignment>()
                .in(EduAssignment::getCourseId, courseIds)
                .eq(EduAssignment::getStatus, 1)
                .orderByDesc(EduAssignment::getDeadline)
                .orderByDesc(EduAssignment::getCreateTime));
        return toAssignmentList(assignments, studentId);
    }

    @Override
    public AssignmentDetailVo studentAssignmentDetail(Long studentId, Long assignmentId) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireStudentCourse(studentId, assignment.getCourseId());
        if (!Integer.valueOf(1).equals(assignment.getStatus())) {
            throw new IllegalStateException("作业尚未发布");
        }
        return toAssignmentDetail(assignment, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAssignment(Long studentId, Long assignmentId, AssignmentSubmitRequest request) {
        EduAssignment assignment = requireAssignment(assignmentId);
        requireStudentCourse(studentId, assignment.getCourseId());
        if (!Integer.valueOf(1).equals(assignment.getStatus())) {
            throw new IllegalStateException("作业尚未发布");
        }
        EduAssignmentSubmission submission = submissionMapper.selectOne(new LambdaQueryWrapper<EduAssignmentSubmission>()
                .eq(EduAssignmentSubmission::getAssignmentId, assignmentId)
                .eq(EduAssignmentSubmission::getStudentId, studentId)
                .last("limit 1"));

        if (submission != null && Integer.valueOf(0).equals(assignment.getAllowResubmit())) {
            throw new IllegalStateException("该作业不允许重复提交");
        }
        if (submission == null) {
            submission = new EduAssignmentSubmission();
            submission.setAssignmentId(assignmentId);
            submission.setCourseId(assignment.getCourseId());
            submission.setStudentId(studentId);
            submission.setSubmitCount(1);
        } else {
            submission.setSubmitCount(submission.getSubmitCount() + 1);
            EduAssignmentReview review = reviewMapper.selectOne(new LambdaQueryWrapper<EduAssignmentReview>()
                    .eq(EduAssignmentReview::getSubmissionId, submission.getId())
                    .last("limit 1"));
            if (review != null) {
                reviewMapper.deleteById(review.getId());
            }
        }
        submission.setContent(request.getContent());
        submission.setFileName(request.getFileName());
        submission.setFileUrl(request.getFileUrl());
        submission.setFileSize(request.getFileSize());
        submission.setFileType(request.getFileType());
        submission.setStatus(1);
        submission.setSubmittedTime(LocalDateTime.now());
        if (submission.getId() == null) {
            submissionMapper.insert(submission);
        } else {
            submissionMapper.updateById(submission);
        }
    }

    private void fillAssignment(EduAssignment assignment, AssignmentSaveRequest request, Long teacherId) {
        assignment.setCourseId(request.getCourseId());
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setTotalScore(request.getTotalScore() == null ? BigDecimal.valueOf(100) : request.getTotalScore());
        assignment.setAllowResubmit(request.getAllowResubmit() == null ? 0 : request.getAllowResubmit());
        assignment.setDeadline(request.getDeadline());
        assignment.setCreateBy(teacherId);
    }

    private List<AssignmentListItemVo> toAssignmentList(List<EduAssignment> assignments, Long studentId) {
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduCourse> courseMap = courseMapper.selectBatchIds(assignments.stream().map(EduAssignment::getCourseId).distinct().toList())
                .stream().collect(Collectors.toMap(EduCourse::getId, Function.identity(), (a, b) -> a));
        Map<Long, EduAssignmentSubmission> submissionMap = studentId == null ? Collections.emptyMap() : submissionMapper.selectList(new LambdaQueryWrapper<EduAssignmentSubmission>()
                        .eq(EduAssignmentSubmission::getStudentId, studentId)
                        .in(EduAssignmentSubmission::getAssignmentId, assignments.stream().map(EduAssignment::getId).toList()))
                .stream().collect(Collectors.toMap(EduAssignmentSubmission::getAssignmentId, Function.identity(), (a, b) -> a));
        Map<Long, EduAssignmentReview> reviewMap = submissionMap.isEmpty() ? Collections.emptyMap() : reviewMapper.selectList(new LambdaQueryWrapper<EduAssignmentReview>()
                        .in(EduAssignmentReview::getSubmissionId, submissionMap.values().stream().map(EduAssignmentSubmission::getId).toList()))
                .stream().collect(Collectors.toMap(EduAssignmentReview::getSubmissionId, Function.identity(), (a, b) -> a));

        return assignments.stream().map(item -> {
            EduAssignmentSubmission submission = submissionMap.get(item.getId());
            EduAssignmentReview review = submission == null ? null : reviewMap.get(submission.getId());
            EduCourse course = courseMap.get(item.getCourseId());
            return AssignmentListItemVo.builder()
                    .id(item.getId())
                    .courseId(item.getCourseId())
                    .courseTitle(course == null ? null : course.getTitle())
                    .title(item.getTitle())
                    .totalScore(item.getTotalScore())
                    .allowResubmit(item.getAllowResubmit())
                    .deadline(item.getDeadline())
                    .status(item.getStatus())
                    .submissionStatus(submission == null ? 0 : submission.getStatus())
                    .submitCount(submission == null ? 0 : submission.getSubmitCount())
                    .reviewScore(review == null ? null : review.getScore())
                    .build();
        }).toList();
    }

    private AssignmentDetailVo toAssignmentDetail(EduAssignment assignment, Long studentId) {
        EduCourse course = courseMapper.selectById(assignment.getCourseId());
        AssignmentSubmissionVo submissionVo = null;
        if (studentId != null) {
            EduAssignmentSubmission submission = submissionMapper.selectOne(new LambdaQueryWrapper<EduAssignmentSubmission>()
                    .eq(EduAssignmentSubmission::getAssignmentId, assignment.getId())
                    .eq(EduAssignmentSubmission::getStudentId, studentId)
                    .last("limit 1"));
            if (submission != null) {
                submissionVo = toSubmissionVo(submission, sysUserMapper.selectById(studentId));
            }
        }
        return AssignmentDetailVo.builder()
                .id(assignment.getId())
                .courseId(assignment.getCourseId())
                .courseTitle(course == null ? null : course.getTitle())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .totalScore(assignment.getTotalScore())
                .allowResubmit(assignment.getAllowResubmit())
                .deadline(assignment.getDeadline())
                .status(assignment.getStatus())
                .submission(submissionVo)
                .build();
    }

    private List<AssignmentSubmissionVo> toSubmissionVos(List<EduAssignmentSubmission> submissions) {
        if (submissions.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(submissions.stream().map(EduAssignmentSubmission::getStudentId).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
        return submissions.stream().map(item -> toSubmissionVo(item, userMap.get(item.getStudentId()))).toList();
    }

    private AssignmentSubmissionVo toSubmissionVo(EduAssignmentSubmission submission, SysUser student) {
        EduAssignmentReview review = reviewMapper.selectOne(new LambdaQueryWrapper<EduAssignmentReview>()
                .eq(EduAssignmentReview::getSubmissionId, submission.getId())
                .last("limit 1"));
        return AssignmentSubmissionVo.builder()
                .submissionId(submission.getId())
                .studentId(submission.getStudentId())
                .studentName(student == null ? null : student.getRealName())
                .content(submission.getContent())
                .fileName(submission.getFileName())
                .fileUrl(submission.getFileUrl())
                .submitCount(submission.getSubmitCount())
                .status(submission.getStatus())
                .submittedTime(submission.getSubmittedTime())
                .score(review == null ? null : review.getScore())
                .comment(review == null ? null : review.getComment())
                .aiComment(review == null ? null : review.getAiComment())
                .reviewTime(review == null ? null : review.getReviewTime())
                .build();
    }

    private EduAssignment requireAssignment(Long assignmentId) {
        EduAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("作业不存在");
        }
        return assignment;
    }

    private EduAssignmentSubmission requireSubmission(Long submissionId) {
        EduAssignmentSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new IllegalArgumentException("提交记录不存在");
        }
        return submission;
    }

    private EduCourse requireTeacherCourse(Long teacherId, Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权操作该课程作业");
        }
        return course;
    }

    private void requireStudentCourse(Long studentId, Long courseId) {
        boolean enrolled = enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .eq(EduCourseEnrollment::getCourseId, courseId)) > 0;
        if (!enrolled) {
            throw new IllegalStateException("请先报名课程");
        }
    }

    private List<Long> teacherCourseIds(Long teacherId, Long courseId) {
        return courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                        .select(EduCourse::getId)
                        .eq(EduCourse::getTeacherId, teacherId)
                        .eq(courseId != null, EduCourse::getId, courseId))
                .stream().map(EduCourse::getId).toList();
    }

    private List<Long> enrolledCourseIds(Long studentId, Long courseId) {
        return enrollmentMapper.selectList(new LambdaQueryWrapper<EduCourseEnrollment>()
                        .select(EduCourseEnrollment::getCourseId)
                        .eq(EduCourseEnrollment::getStudentId, studentId)
                        .eq(courseId != null, EduCourseEnrollment::getCourseId, courseId))
                .stream().map(EduCourseEnrollment::getCourseId).toList();
    }
}
