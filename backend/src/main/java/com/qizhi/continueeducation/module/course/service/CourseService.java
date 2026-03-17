package com.qizhi.continueeducation.module.course.service;

import com.qizhi.continueeducation.module.course.dto.ChapterSaveRequest;
import com.qizhi.continueeducation.module.course.dto.CourseAuditRequest;
import com.qizhi.continueeducation.module.course.dto.CourseSaveRequest;
import com.qizhi.continueeducation.module.course.dto.LessonSaveRequest;
import com.qizhi.continueeducation.module.course.vo.CourseCategoryVo;
import com.qizhi.continueeducation.module.course.vo.CourseChapterVo;
import com.qizhi.continueeducation.module.course.vo.CourseDetailVo;
import com.qizhi.continueeducation.module.course.vo.CourseLessonVo;
import com.qizhi.continueeducation.module.course.vo.CourseListItemVo;
import com.qizhi.continueeducation.module.learning.vo.LearningOverviewVo;

import java.util.List;

public interface CourseService {

    List<CourseCategoryVo> listCategories();

    List<CourseListItemVo> publicCourses(Long studentId, String keyword, Long categoryId);

    CourseDetailVo publicCourseDetail(Long studentId, Long courseId);

    void enrollCourse(Long studentId, Long courseId);

    List<CourseListItemVo> studentCourses(Long studentId, Integer status);

    LearningOverviewVo studentCourseLearningOverview(Long studentId, Long courseId);

    List<CourseListItemVo> teacherCourses(Long teacherId, String keyword, Integer auditStatus, Integer status);

    CourseDetailVo teacherCourseDetail(Long teacherId, Long courseId);

    Long createCourse(Long teacherId, CourseSaveRequest request);

    void updateCourse(Long teacherId, Long courseId, CourseSaveRequest request);

    void submitAudit(Long teacherId, Long courseId);

    Long createChapter(Long teacherId, Long courseId, ChapterSaveRequest request);

    void updateChapter(Long teacherId, Long courseId, Long chapterId, ChapterSaveRequest request);

    void deleteChapter(Long teacherId, Long courseId, Long chapterId);

    Long createLesson(Long teacherId, Long courseId, LessonSaveRequest request);

    void updateLesson(Long teacherId, Long courseId, Long lessonId, LessonSaveRequest request);

    void deleteLesson(Long teacherId, Long courseId, Long lessonId);

    List<CourseListItemVo> adminCourses(String keyword, Integer auditStatus, Integer status, Long teacherId);

    CourseDetailVo adminCourseDetail(Long courseId);

    void auditCourse(Long adminId, Long courseId, CourseAuditRequest request);
}
