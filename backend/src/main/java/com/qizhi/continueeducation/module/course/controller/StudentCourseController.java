package com.qizhi.continueeducation.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.course.vo.CourseDetailVo;
import com.qizhi.continueeducation.module.course.vo.CourseListItemVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseService courseService;

    @GetMapping("/api/public/courses")
    public ApiResponse<List<CourseListItemVo>> publicCourses(@RequestParam(required = false) String keyword,
                                                             @RequestParam(required = false) Long categoryId) {
        Long studentId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ApiResponse.success(courseService.publicCourses(studentId, keyword, categoryId));
    }

    @GetMapping("/api/public/courses/{courseId}")
    public ApiResponse<CourseDetailVo> publicCourseDetail(@PathVariable Long courseId) {
        Long studentId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ApiResponse.success(courseService.publicCourseDetail(studentId, courseId));
    }

    @PostMapping("/api/student/courses/{courseId}/enroll")
    public ApiResponse<Void> enroll(@PathVariable Long courseId) {
        StpUtil.checkRole(RoleCode.STUDENT);
        courseService.enrollCourse(StpUtil.getLoginIdAsLong(), courseId);
        return ApiResponse.success("报名成功", null);
    }

    @GetMapping("/api/student/courses")
    public ApiResponse<List<CourseListItemVo>> myCourses(@RequestParam(required = false) Integer status) {
        StpUtil.checkRole(RoleCode.STUDENT);
        return ApiResponse.success(courseService.studentCourses(StpUtil.getLoginIdAsLong(), status));
    }
}
