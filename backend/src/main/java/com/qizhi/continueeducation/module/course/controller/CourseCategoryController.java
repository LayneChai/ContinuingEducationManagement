package com.qizhi.continueeducation.module.course.controller;

import com.qizhi.continueeducation.common.domain.ApiResponse;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.course.vo.CourseCategoryVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/course")
public class CourseCategoryController {

    private final CourseService courseService;

    @GetMapping("/categories")
    public ApiResponse<List<CourseCategoryVo>> categories() {
        return ApiResponse.success(courseService.listCategories());
    }
}
