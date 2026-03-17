package com.qizhi.continueeducation.module.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.exam.dto.ExamQuestionSaveRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSaveRequest;
import com.qizhi.continueeducation.module.exam.service.ExamService;
import com.qizhi.continueeducation.module.exam.vo.ExamDetailVo;
import com.qizhi.continueeducation.module.exam.vo.ExamListItemVo;
import com.qizhi.continueeducation.module.exam.vo.ExamQuestionVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TeacherExamController {

    private final ExamService examService;

    @GetMapping("/api/teacher/questions")
    public ApiResponse<List<ExamQuestionVo>> questions(@RequestParam Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(examService.teacherQuestionList(StpUtil.getLoginIdAsLong(), courseId));
    }

    @PostMapping("/api/teacher/questions")
    public ApiResponse<Map<String, Long>> createQuestion(@Valid @RequestBody ExamQuestionSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("questionId", examService.createQuestion(StpUtil.getLoginIdAsLong(), request)));
    }

    @PutMapping("/api/teacher/questions/{questionId}")
    public ApiResponse<Void> updateQuestion(@PathVariable Long questionId, @Valid @RequestBody ExamQuestionSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        examService.updateQuestion(StpUtil.getLoginIdAsLong(), questionId, request);
        return ApiResponse.success("题目更新成功", null);
    }

    @DeleteMapping("/api/teacher/questions/{questionId}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long questionId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        examService.deleteQuestion(StpUtil.getLoginIdAsLong(), questionId);
        return ApiResponse.success("题目删除成功", null);
    }

    @GetMapping("/api/teacher/exams")
    public ApiResponse<List<ExamListItemVo>> exams(@RequestParam(required = false) Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(examService.teacherExamList(StpUtil.getLoginIdAsLong(), courseId));
    }

    @PostMapping("/api/teacher/exams")
    public ApiResponse<Map<String, Long>> createExam(@Valid @RequestBody ExamSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("examId", examService.createExam(StpUtil.getLoginIdAsLong(), request)));
    }

    @PutMapping("/api/teacher/exams/{examId}")
    public ApiResponse<Void> updateExam(@PathVariable Long examId, @Valid @RequestBody ExamSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        examService.updateExam(StpUtil.getLoginIdAsLong(), examId, request);
        return ApiResponse.success("考试更新成功", null);
    }

    @GetMapping("/api/teacher/exams/{examId}")
    public ApiResponse<ExamDetailVo> examDetail(@PathVariable Long examId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(examService.teacherExamDetail(StpUtil.getLoginIdAsLong(), examId));
    }

    @PostMapping("/api/teacher/exams/{examId}/publish")
    public ApiResponse<Void> publishExam(@PathVariable Long examId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        examService.publishExam(StpUtil.getLoginIdAsLong(), examId);
        return ApiResponse.success("考试已发布", null);
    }
}
