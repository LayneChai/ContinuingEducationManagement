package com.qizhi.continueeducation.module.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.exam.dto.ExamSubmitRequest;
import com.qizhi.continueeducation.module.exam.service.ExamService;
import com.qizhi.continueeducation.module.exam.vo.ExamDetailVo;
import com.qizhi.continueeducation.module.exam.vo.ExamListItemVo;
import com.qizhi.continueeducation.module.exam.vo.ExamSubmitResultVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentExamController {

    private final ExamService examService;

    @GetMapping("/api/student/exams")
    public ApiResponse<List<ExamListItemVo>> exams(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(examService.studentExamList(StpUtil.getLoginIdAsLong(), courseId));
    }

    @GetMapping("/api/student/exams/{examId}")
    public ApiResponse<ExamDetailVo> detail(@PathVariable Long examId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(examService.studentExamDetail(StpUtil.getLoginIdAsLong(), examId));
    }

    @PostMapping("/api/student/exams/{examId}/submit")
    public ApiResponse<ExamSubmitResultVo> submit(@PathVariable Long examId, @Valid @RequestBody ExamSubmitRequest request) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success("交卷成功", examService.submitExam(StpUtil.getLoginIdAsLong(), examId, request));
    }
}
