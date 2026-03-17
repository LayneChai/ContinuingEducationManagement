package com.qizhi.continueeducation.module.assignment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentReviewRequest;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSaveRequest;
import com.qizhi.continueeducation.module.assignment.service.AssignmentService;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentDetailVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentListItemVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentSubmissionVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/api/teacher/assignments")
    public ApiResponse<List<AssignmentListItemVo>> list(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(assignmentService.teacherAssignments(StpUtil.getLoginIdAsLong(), courseId));
    }

    @PostMapping("/api/teacher/assignments")
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody AssignmentSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("assignmentId", assignmentService.createAssignment(StpUtil.getLoginIdAsLong(), request)));
    }

    @PutMapping("/api/teacher/assignments/{assignmentId}")
    public ApiResponse<Void> update(@PathVariable Long assignmentId, @Valid @RequestBody AssignmentSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        assignmentService.updateAssignment(StpUtil.getLoginIdAsLong(), assignmentId, request);
        return ApiResponse.success("作业更新成功", null);
    }

    @PostMapping("/api/teacher/assignments/{assignmentId}/publish")
    public ApiResponse<Void> publish(@PathVariable Long assignmentId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        assignmentService.publishAssignment(StpUtil.getLoginIdAsLong(), assignmentId);
        return ApiResponse.success("作业已发布", null);
    }

    @GetMapping("/api/teacher/assignments/{assignmentId}")
    public ApiResponse<AssignmentDetailVo> detail(@PathVariable Long assignmentId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(assignmentService.teacherAssignmentDetail(StpUtil.getLoginIdAsLong(), assignmentId));
    }

    @GetMapping("/api/teacher/assignments/{assignmentId}/submissions")
    public ApiResponse<List<AssignmentSubmissionVo>> submissions(@PathVariable Long assignmentId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(assignmentService.teacherSubmissions(StpUtil.getLoginIdAsLong(), assignmentId));
    }

    @PostMapping("/api/teacher/submissions/{submissionId}/review")
    public ApiResponse<Void> review(@PathVariable Long submissionId, @Valid @RequestBody AssignmentReviewRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        assignmentService.reviewSubmission(StpUtil.getLoginIdAsLong(), submissionId, request);
        return ApiResponse.success("批改完成", null);
    }
}
