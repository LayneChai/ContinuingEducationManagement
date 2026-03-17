package com.qizhi.continueeducation.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.course.dto.CourseAuditRequest;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.course.vo.CourseDetailVo;
import com.qizhi.continueeducation.module.course.vo.CourseListItemVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<List<CourseListItemVo>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Integer auditStatus,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Long teacherId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(courseService.adminCourses(keyword, auditStatus, status, teacherId));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailVo> detail(@PathVariable Long courseId) {
        StpUtil.checkRole(RoleCode.ADMIN);
        return ApiResponse.success(courseService.adminCourseDetail(courseId));
    }

    @PostMapping("/{courseId}/audit")
    public ApiResponse<Void> audit(@PathVariable Long courseId, @Valid @RequestBody CourseAuditRequest request) {
        StpUtil.checkRole(RoleCode.ADMIN);
        courseService.auditCourse(StpUtil.getLoginIdAsLong(), courseId, request);
        return ApiResponse.success("审核完成", null);
    }
}
