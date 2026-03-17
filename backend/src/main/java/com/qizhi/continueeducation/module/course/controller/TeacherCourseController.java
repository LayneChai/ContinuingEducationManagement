package com.qizhi.continueeducation.module.course.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.course.dto.ChapterSaveRequest;
import com.qizhi.continueeducation.module.course.dto.CourseSaveRequest;
import com.qizhi.continueeducation.module.course.dto.LessonSaveRequest;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.course.vo.CourseDetailVo;
import com.qizhi.continueeducation.module.course.vo.CourseListItemVo;
import com.qizhi.continueeducation.module.system.enums.RoleCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teacher/courses")
public class TeacherCourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<List<CourseListItemVo>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Integer auditStatus,
                                                    @RequestParam(required = false) Integer status) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(courseService.teacherCourses(StpUtil.getLoginIdAsLong(), keyword, auditStatus, status));
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseDetailVo> detail(@PathVariable Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(courseService.teacherCourseDetail(StpUtil.getLoginIdAsLong(), courseId));
    }

    @PostMapping
    public ApiResponse<Map<String, Long>> create(@Valid @RequestBody CourseSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("courseId", courseService.createCourse(StpUtil.getLoginIdAsLong(), request)));
    }

    @PutMapping("/{courseId}")
    public ApiResponse<Void> update(@PathVariable Long courseId, @Valid @RequestBody CourseSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.updateCourse(StpUtil.getLoginIdAsLong(), courseId, request);
        return ApiResponse.success("保存成功", null);
    }

    @PostMapping("/{courseId}/submit-audit")
    public ApiResponse<Void> submitAudit(@PathVariable Long courseId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.submitAudit(StpUtil.getLoginIdAsLong(), courseId);
        return ApiResponse.success("已提交审核", null);
    }

    @PostMapping("/{courseId}/chapters")
    public ApiResponse<Map<String, Long>> createChapter(@PathVariable Long courseId, @Valid @RequestBody ChapterSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("chapterId", courseService.createChapter(StpUtil.getLoginIdAsLong(), courseId, request)));
    }

    @PutMapping("/{courseId}/chapters/{chapterId}")
    public ApiResponse<Void> updateChapter(@PathVariable Long courseId, @PathVariable Long chapterId,
                                           @Valid @RequestBody ChapterSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.updateChapter(StpUtil.getLoginIdAsLong(), courseId, chapterId, request);
        return ApiResponse.success("章节更新成功", null);
    }

    @DeleteMapping("/{courseId}/chapters/{chapterId}")
    public ApiResponse<Void> deleteChapter(@PathVariable Long courseId, @PathVariable Long chapterId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.deleteChapter(StpUtil.getLoginIdAsLong(), courseId, chapterId);
        return ApiResponse.success("章节删除成功", null);
    }

    @PostMapping("/{courseId}/lessons")
    public ApiResponse<Map<String, Long>> createLesson(@PathVariable Long courseId, @Valid @RequestBody LessonSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        return ApiResponse.success(Map.of("lessonId", courseService.createLesson(StpUtil.getLoginIdAsLong(), courseId, request)));
    }

    @PutMapping("/{courseId}/lessons/{lessonId}")
    public ApiResponse<Void> updateLesson(@PathVariable Long courseId, @PathVariable Long lessonId,
                                          @Valid @RequestBody LessonSaveRequest request) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.updateLesson(StpUtil.getLoginIdAsLong(), courseId, lessonId, request);
        return ApiResponse.success("课时更新成功", null);
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    public ApiResponse<Void> deleteLesson(@PathVariable Long courseId, @PathVariable Long lessonId) {
        StpUtil.checkRole(RoleCode.TEACHER);
        courseService.deleteLesson(StpUtil.getLoginIdAsLong(), courseId, lessonId);
        return ApiResponse.success("课时删除成功", null);
    }
}
