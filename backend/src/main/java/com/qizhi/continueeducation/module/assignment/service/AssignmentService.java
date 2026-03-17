package com.qizhi.continueeducation.module.assignment.service;

import com.qizhi.continueeducation.module.assignment.dto.AssignmentReviewRequest;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSaveRequest;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSubmitRequest;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentDetailVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentListItemVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentSubmissionVo;

import java.util.List;

public interface AssignmentService {
    List<AssignmentListItemVo> teacherAssignments(Long teacherId, Long courseId);
    Long createAssignment(Long teacherId, AssignmentSaveRequest request);
    void updateAssignment(Long teacherId, Long assignmentId, AssignmentSaveRequest request);
    void publishAssignment(Long teacherId, Long assignmentId);
    AssignmentDetailVo teacherAssignmentDetail(Long teacherId, Long assignmentId);
    List<AssignmentSubmissionVo> teacherSubmissions(Long teacherId, Long assignmentId);
    void reviewSubmission(Long teacherId, Long submissionId, AssignmentReviewRequest request);

    List<AssignmentListItemVo> studentAssignments(Long studentId, Long courseId);
    AssignmentDetailVo studentAssignmentDetail(Long studentId, Long assignmentId);
    void submitAssignment(Long studentId, Long assignmentId, AssignmentSubmitRequest request);
}
