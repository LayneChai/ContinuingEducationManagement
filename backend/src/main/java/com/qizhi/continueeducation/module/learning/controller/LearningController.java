package com.qizhi.continueeducation.module.learning.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.learning.dto.LessonStudyRequest;
import com.qizhi.continueeducation.module.learning.service.LearningService;
import com.qizhi.continueeducation.module.learning.vo.LearningOverviewVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student/courses/{courseId}")
public class LearningController {

    private final LearningService learningService;
    private final CourseService courseService;

    @PostMapping("/lessons/{lessonId}/study")
    public ApiResponse<Void> studyLesson(@PathVariable Long courseId,
                                         @PathVariable Long lessonId,
                                         @Valid @RequestBody LessonStudyRequest request) {
        StpUtil.checkRole(RoleCode.STUDENT);
        learningService.studyLesson(StpUtil.getLoginIdAsLong(), courseId, lessonId, request);
        return ApiResponse.success("学习进度已更新", null);
    }

    @GetMapping("/learning-overview")
    public ApiResponse<LearningOverviewVo> learningOverview(@PathVariable Long courseId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(courseService.studentCourseLearningOverview(StpUtil.getLoginIdAsLong(), courseId));
    }
}
