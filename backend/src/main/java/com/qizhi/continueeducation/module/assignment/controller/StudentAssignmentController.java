package com.qizhi.continueeducation.module.assignment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.assignment.dto.AssignmentSubmitRequest;
import com.qizhi.continueeducation.module.assignment.service.AssignmentService;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentDetailVo;
import com.qizhi.continueeducation.module.assignment.vo.AssignmentListItemVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentAssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/api/student/assignments")
    public ApiResponse<List<AssignmentListItemVo>> list(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(assignmentService.studentAssignments(StpUtil.getLoginIdAsLong(), courseId));
    }

    @GetMapping("/api/student/assignments/{assignmentId}")
    public ApiResponse<AssignmentDetailVo> detail(@PathVariable Long assignmentId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(assignmentService.studentAssignmentDetail(StpUtil.getLoginIdAsLong(), assignmentId));
    }

    @PostMapping("/api/student/assignments/{assignmentId}/submit")
    public ApiResponse<Void> submit(@PathVariable Long assignmentId, @RequestBody AssignmentSubmitRequest request) {
        StpUtil.checkRole(RoleCode.STUDENT);
        assignmentService.submitAssignment(StpUtil.getLoginIdAsLong(), assignmentId, request);
        return ApiResponse.success("提交成功", null);
    }
}
