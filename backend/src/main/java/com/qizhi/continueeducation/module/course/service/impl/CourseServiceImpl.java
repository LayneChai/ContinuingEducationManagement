package com.qizhi.continueeducation.module.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qizhi.continueeducation.module.course.dto.ChapterSaveRequest;
import com.qizhi.continueeducation.module.course.dto.CourseAuditRequest;
import com.qizhi.continueeducation.module.course.dto.CourseSaveRequest;
import com.qizhi.continueeducation.module.course.dto.LessonSaveRequest;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseCategory;
import com.qizhi.continueeducation.module.course.entity.EduCourseChapter;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.entity.EduCourseLesson;
import com.qizhi.continueeducation.module.course.mapper.EduCourseCategoryMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseChapterMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseLessonMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.course.service.CourseService;
import com.qizhi.continueeducation.module.course.vo.CourseCategoryVo;
import com.qizhi.continueeducation.module.course.vo.CourseChapterVo;
import com.qizhi.continueeducation.module.course.vo.CourseDetailVo;
import com.qizhi.continueeducation.module.course.vo.CourseLessonVo;
import com.qizhi.continueeducation.module.course.vo.CourseListItemVo;
import com.qizhi.continueeducation.module.learning.service.LearningService;
import com.qizhi.continueeducation.module.learning.vo.LearningOverviewVo;
import com.qizhi.continueeducation.module.system.entity.SysUser;
import com.qizhi.continueeducation.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final EduCourseMapper courseMapper;
    private final EduCourseCategoryMapper categoryMapper;
    private final EduCourseChapterMapper chapterMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final EduCourseLessonMapper lessonMapper;
    private final LearningService learningService;
    private final SysUserMapper sysUserMapper;

    @Override
    public List<CourseCategoryVo> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<EduCourseCategory>()
                        .eq(EduCourseCategory::getStatus, 1)
                        .orderByAsc(EduCourseCategory::getSort))
                .stream()
                .map(item -> CourseCategoryVo.builder()
                        .id(item.getId())
                        .parentId(item.getParentId())
                        .name(item.getName())
                        .code(item.getCode())
                        .build())
                .toList();
    }

    @Override
    public List<CourseListItemVo> publicCourses(Long studentId, String keyword, Long categoryId) {
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getAuditStatus, 1)
                .eq(EduCourse::getStatus, 1)
                .orderByDesc(EduCourse::getPublishTime)
                .orderByDesc(EduCourse::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(EduCourse::getTitle, keyword).or().like(EduCourse::getCourseCode, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(EduCourse::getCategoryId, categoryId);
        }
        return toCourseListVo(courseMapper.selectList(wrapper), studentId);
    }

    @Override
    public CourseDetailVo publicCourseDetail(Long studentId, Long courseId) {
        EduCourse course = requireCourse(courseId);
        if (!Integer.valueOf(1).equals(course.getAuditStatus()) || !Integer.valueOf(1).equals(course.getStatus())) {
            throw new IllegalArgumentException("课程未开放报名");
        }
        return toCourseDetailVo(course, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enrollCourse(Long studentId, Long courseId) {
        EduCourse course = requireCourse(courseId);
        if (!Integer.valueOf(1).equals(course.getAuditStatus()) || !Integer.valueOf(1).equals(course.getStatus())) {
            throw new IllegalArgumentException("课程暂不可报名");
        }
        EduCourseEnrollment enrollment = enrollmentMapper.selectOne(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getCourseId, courseId)
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .last("limit 1"));
        if (enrollment != null) {
            if (Integer.valueOf(3).equals(enrollment.getStatus())) {
                enrollment.setStatus(0);
                enrollment.setProgressPercent(BigDecimal.ZERO);
                enrollment.setCompletedTime(null);
                enrollmentMapper.updateById(enrollment);
                return;
            }
            throw new IllegalStateException("你已报名该课程");
        }
        enrollment = new EduCourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setEnrollTime(LocalDateTime.now());
        enrollment.setStatus(0);
        enrollment.setProgressPercent(BigDecimal.ZERO);
        enrollment.setSource("student-self");
        enrollmentMapper.insert(enrollment);
    }

    @Override
    public List<CourseListItemVo> studentCourses(Long studentId, Integer status) {
        List<EduCourseEnrollment> enrollments = enrollmentMapper.selectList(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .eq(status != null, EduCourseEnrollment::getStatus, status)
                .orderByDesc(EduCourseEnrollment::getEnrollTime));
        if (enrollments.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduCourseEnrollment> enrollmentMap = enrollments.stream()
                .collect(Collectors.toMap(EduCourseEnrollment::getCourseId, Function.identity(), (a, b) -> a));
        List<EduCourse> courses = courseMapper.selectBatchIds(enrollmentMap.keySet());
        return toCourseListVo(courses, enrollmentMap);
    }

    @Override
    public LearningOverviewVo studentCourseLearningOverview(Long studentId, Long courseId) {
        return learningService.getCourseLearningOverview(studentId, courseId);
    }

    @Override
    public List<CourseListItemVo> teacherCourses(Long teacherId, String keyword, Integer auditStatus, Integer status) {
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>()
                .eq(EduCourse::getTeacherId, teacherId)
                .orderByDesc(EduCourse::getCreateTime);
        applyCourseFilters(wrapper, keyword, auditStatus, status, null);
        return toCourseListVo(courseMapper.selectList(wrapper));
    }

    @Override
    public CourseDetailVo teacherCourseDetail(Long teacherId, Long courseId) {
        EduCourse course = requireCourse(courseId);
        if (!teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权访问该课程");
        }
        return toCourseDetailVo(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(Long teacherId, CourseSaveRequest request) {
        validateCategory(request.getCategoryId());
        EduCourse course = new EduCourse();
        fillCourse(course, teacherId, request);
        course.setCourseCode("COURSE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        course.setAuditStatus(0);
        course.setStatus(0);
        course.setTotalLessons(0);
        courseMapper.insert(course);
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long teacherId, Long courseId, CourseSaveRequest request) {
        validateCategory(request.getCategoryId());
        EduCourse course = requireTeacherCourse(teacherId, courseId);
        fillCourse(course, teacherId, request);
        if (Integer.valueOf(1).equals(course.getAuditStatus())) {
            course.setAuditStatus(0);
            course.setStatus(0);
            course.setAuditRemark("课程更新后待重新审核");
        }
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Long teacherId, Long courseId) {
        EduCourse course = requireTeacherCourse(teacherId, courseId);
        long chapterCount = chapterMapper.selectCount(new LambdaQueryWrapper<EduCourseChapter>().eq(EduCourseChapter::getCourseId, courseId));
        long lessonCount = lessonMapper.selectCount(new LambdaQueryWrapper<EduCourseLesson>().eq(EduCourseLesson::getCourseId, courseId));
        if (chapterCount == 0 || lessonCount == 0) {
            throw new IllegalStateException("请至少完善一个章节和一个课时后再提交审核");
        }
        course.setAuditStatus(0);
        course.setStatus(0);
        course.setAuditRemark(null);
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createChapter(Long teacherId, Long courseId, ChapterSaveRequest request) {
        requireTeacherCourse(teacherId, courseId);
        EduCourseChapter chapter = new EduCourseChapter();
        chapter.setCourseId(courseId);
        chapter.setTitle(request.getTitle());
        chapter.setSort(defaultInt(request.getSort()));
        chapter.setDescription(request.getDescription());
        chapterMapper.insert(chapter);
        return chapter.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChapter(Long teacherId, Long courseId, Long chapterId, ChapterSaveRequest request) {
        requireTeacherCourse(teacherId, courseId);
        EduCourseChapter chapter = requireChapter(courseId, chapterId);
        chapter.setTitle(request.getTitle());
        chapter.setSort(defaultInt(request.getSort()));
        chapter.setDescription(request.getDescription());
        chapterMapper.updateById(chapter);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapter(Long teacherId, Long courseId, Long chapterId) {
        requireTeacherCourse(teacherId, courseId);
        requireChapter(courseId, chapterId);
        lessonMapper.delete(new LambdaQueryWrapper<EduCourseLesson>().eq(EduCourseLesson::getChapterId, chapterId));
        chapterMapper.deleteById(chapterId);
        refreshTotalLessons(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLesson(Long teacherId, Long courseId, LessonSaveRequest request) {
        requireTeacherCourse(teacherId, courseId);
        requireChapter(courseId, request.getChapterId());
        EduCourseLesson lesson = new EduCourseLesson();
        fillLesson(courseId, lesson, request);
        lessonMapper.insert(lesson);
        refreshTotalLessons(courseId);
        return lesson.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLesson(Long teacherId, Long courseId, Long lessonId, LessonSaveRequest request) {
        requireTeacherCourse(teacherId, courseId);
        requireChapter(courseId, request.getChapterId());
        EduCourseLesson lesson = requireLesson(courseId, lessonId);
        fillLesson(courseId, lesson, request);
        lessonMapper.updateById(lesson);
        refreshTotalLessons(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLesson(Long teacherId, Long courseId, Long lessonId) {
        requireTeacherCourse(teacherId, courseId);
        requireLesson(courseId, lessonId);
        lessonMapper.deleteById(lessonId);
        refreshTotalLessons(courseId);
    }

    @Override
    public List<CourseListItemVo> adminCourses(String keyword, Integer auditStatus, Integer status, Long teacherId) {
        LambdaQueryWrapper<EduCourse> wrapper = new LambdaQueryWrapper<EduCourse>().orderByDesc(EduCourse::getCreateTime);
        applyCourseFilters(wrapper, keyword, auditStatus, status, teacherId);
        return toCourseListVo(courseMapper.selectList(wrapper));
    }

    @Override
    public CourseDetailVo adminCourseDetail(Long courseId) {
        return toCourseDetailVo(requireCourse(courseId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCourse(Long adminId, Long courseId, CourseAuditRequest request) {
        EduCourse course = requireCourse(courseId);
        course.setAuditStatus(request.getAuditStatus());
        course.setAuditRemark(request.getAuditRemark());
        if (Integer.valueOf(1).equals(request.getAuditStatus())) {
            course.setStatus(1);
            course.setPublishTime(LocalDateTime.now());
        } else {
            course.setStatus(2);
        }
        courseMapper.updateById(course);
    }

    private void applyCourseFilters(LambdaQueryWrapper<EduCourse> wrapper, String keyword, Integer auditStatus, Integer status, Long teacherId) {
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(EduCourse::getTitle, keyword).or().like(EduCourse::getCourseCode, keyword));
        }
        if (auditStatus != null) {
            wrapper.eq(EduCourse::getAuditStatus, auditStatus);
        }
        if (status != null) {
            wrapper.eq(EduCourse::getStatus, status);
        }
        if (teacherId != null) {
            wrapper.eq(EduCourse::getTeacherId, teacherId);
        }
    }

    private List<CourseListItemVo> toCourseListVo(List<EduCourse> courses) {
        return toCourseListVo(courses, Collections.emptyMap());
    }

    private List<CourseListItemVo> toCourseListVo(List<EduCourse> courses, Long studentId) {
        if (studentId == null) {
            return toCourseListVo(courses, Collections.emptyMap());
        }
        List<EduCourseEnrollment> enrollments = courses.isEmpty() ? List.of() : enrollmentMapper.selectList(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .in(EduCourseEnrollment::getCourseId, courses.stream().map(EduCourse::getId).toList()));
        Map<Long, EduCourseEnrollment> enrollmentMap = enrollments.stream()
                .collect(Collectors.toMap(EduCourseEnrollment::getCourseId, Function.identity(), (a, b) -> a));
        return toCourseListVo(courses, enrollmentMap);
    }

    private List<CourseListItemVo> toCourseListVo(List<EduCourse> courses, Map<Long, EduCourseEnrollment> enrollmentMap) {
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, BigDecimal> requiredHoursMap = getRequiredHoursMap(courses.stream().map(EduCourse::getId).toList());
        Map<Long, String> teacherMap = sysUserMapper.selectBatchIds(courses.stream().map(EduCourse::getTeacherId).distinct().toList())
                .stream().collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
        List<Long> categoryIds = courses.stream().map(EduCourse::getCategoryId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> categoryMap = categoryIds.isEmpty()
                ? Collections.emptyMap()
                : categoryMapper.selectBatchIds(categoryIds)
                        .stream().collect(Collectors.toMap(EduCourseCategory::getId, EduCourseCategory::getName, (a, b) -> a));
        return courses.stream().map(course -> CourseListItemVo.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .teacherId(course.getTeacherId())
                .teacherName(teacherMap.get(course.getTeacherId()))
                .categoryId(course.getCategoryId())
                .categoryName(categoryMap.get(course.getCategoryId()))
                .requiredHours(resolveRequiredHours(course.getId(), requiredHoursMap))
                .totalLessons(course.getTotalLessons())
                .auditStatus(course.getAuditStatus())
                .status(course.getStatus())
                .auditRemark(course.getAuditRemark())
                .publishTime(course.getPublishTime())
                .createTime(course.getCreateTime())
                .enrolled(enrollmentMap.containsKey(course.getId()))
                .enrollmentStatus(enrollmentMap.containsKey(course.getId()) ? enrollmentMap.get(course.getId()).getStatus() : null)
                .learningProgress(enrollmentMap.containsKey(course.getId()) ? enrollmentMap.get(course.getId()).getProgressPercent() : null)
                .build()).toList();
    }

    private CourseDetailVo toCourseDetailVo(EduCourse course) {
        return toCourseDetailVo(course, null);
    }

    private CourseDetailVo toCourseDetailVo(EduCourse course, Long studentId) {
        SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
        EduCourseCategory category = course.getCategoryId() == null ? null : categoryMapper.selectById(course.getCategoryId());
        EduCourseEnrollment enrollment = studentId == null ? null : enrollmentMapper.selectOne(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getCourseId, course.getId())
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .last("limit 1"));
        Map<Long, BigDecimal> lessonProgressMap = (studentId == null || enrollment == null)
                ? Collections.emptyMap()
                : learningService.getLessonProgressMap(studentId, course.getId());
        LearningOverviewVo overview = (studentId == null || enrollment == null)
                ? null
                : learningService.getCourseLearningOverview(studentId, course.getId());
        List<EduCourseChapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<EduCourseChapter>()
                .eq(EduCourseChapter::getCourseId, course.getId())
                .orderByAsc(EduCourseChapter::getSort)
                .orderByAsc(EduCourseChapter::getId));
        List<EduCourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<EduCourseLesson>()
                .eq(EduCourseLesson::getCourseId, course.getId())
                .orderByAsc(EduCourseLesson::getSort)
                .orderByAsc(EduCourseLesson::getId));
        BigDecimal requiredHours = calculateRequiredHours(lessons);
        Map<Long, List<CourseLessonVo>> lessonMap = lessons.stream().collect(Collectors.groupingBy(EduCourseLesson::getChapterId,
                Collectors.mapping(item -> toLessonVo(item, lessonProgressMap.get(item.getId())), Collectors.toList())));

        List<CourseChapterVo> chapterVos = chapters.stream()
                .sorted(Comparator.comparing(EduCourseChapter::getSort).thenComparing(EduCourseChapter::getId))
                .map(chapter -> CourseChapterVo.builder()
                        .id(chapter.getId())
                        .courseId(chapter.getCourseId())
                        .title(chapter.getTitle())
                        .sort(chapter.getSort())
                        .description(chapter.getDescription())
                        .lessons(lessonMap.getOrDefault(chapter.getId(), List.of()))
                        .build())
                .toList();

        return CourseDetailVo.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .subtitle(course.getSubtitle())
                .coverUrl(course.getCoverUrl())
                .teacherId(course.getTeacherId())
                .teacherName(teacher == null ? null : teacher.getRealName())
                .categoryId(course.getCategoryId())
                .categoryName(category == null ? null : category.getName())
                .description(course.getDescription())
                .targetUser(course.getTargetUser())
                .requiredHours(requiredHours)
                .totalLessons(course.getTotalLessons())
                .examRequired(course.getExamRequired())
                .assignmentRequired(course.getAssignmentRequired())
                .certificateEnabled(course.getCertificateEnabled())
                .auditStatus(course.getAuditStatus())
                .status(course.getStatus())
                .auditRemark(course.getAuditRemark())
                .publishTime(course.getPublishTime())
                .enrolled(enrollment != null)
                .enrollmentStatus(enrollment == null ? null : enrollment.getStatus())
                .learningProgress(overview == null ? (enrollment == null ? null : enrollment.getProgressPercent()) : overview.getCompletionRate())
                .completedHours(overview == null ? null : overview.getCompletedHours())
                .qualified(overview == null ? null : overview.getQualified())
                .completedLessons(overview == null ? null : overview.getCompletedLessons())
                .chapters(chapterVos)
                .build();
    }

    private CourseLessonVo toLessonVo(EduCourseLesson lesson) {
        return toLessonVo(lesson, null);
    }

    private CourseLessonVo toLessonVo(EduCourseLesson lesson, BigDecimal progress) {
        return CourseLessonVo.builder()
                .id(lesson.getId())
                .chapterId(lesson.getChapterId())
                .title(lesson.getTitle())
                .lessonType(lesson.getLessonType())
                .resourceUrl(lesson.getResourceUrl())
                .content(lesson.getContent())
                .durationSeconds(lesson.getDurationSeconds())
                .previewEnabled(lesson.getPreviewEnabled())
                .sort(lesson.getSort())
                .status(lesson.getStatus())
                .learningProgress(progress)
                .completed(progress != null && progress.compareTo(BigDecimal.valueOf(100)) >= 0)
                .build();
    }

    private void fillCourse(EduCourse course, Long teacherId, CourseSaveRequest request) {
        course.setTeacherId(teacherId);
        course.setTitle(request.getTitle());
        course.setSubtitle(request.getSubtitle());
        course.setCoverUrl(request.getCoverUrl());
        course.setCategoryId(request.getCategoryId());
        course.setDescription(request.getDescription());
        course.setTargetUser(request.getTargetUser());
        course.setRequiredHours(BigDecimal.ZERO);
        course.setExamRequired(defaultBinary(request.getExamRequired()));
        course.setAssignmentRequired(defaultBinary(request.getAssignmentRequired()));
        course.setCertificateEnabled(request.getCertificateEnabled() == null ? 1 : request.getCertificateEnabled());
    }

    private void fillLesson(Long courseId, EduCourseLesson lesson, LessonSaveRequest request) {
        lesson.setCourseId(courseId);
        lesson.setChapterId(request.getChapterId());
        lesson.setTitle(request.getTitle());
        lesson.setLessonType(request.getLessonType());
        lesson.setResourceUrl(request.getResourceUrl());
        lesson.setContent(request.getContent());
        lesson.setDurationSeconds(defaultInt(request.getDurationSeconds()));
        lesson.setPreviewEnabled(defaultBinary(request.getPreviewEnabled()));
        lesson.setSort(defaultInt(request.getSort()));
        lesson.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private void validateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("课程分类不能为空");
        }
        EduCourseCategory category = categoryMapper.selectById(categoryId);
        if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
            throw new IllegalArgumentException("课程分类不存在或已禁用");
        }
    }

    private EduCourse requireCourse(Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        return course;
    }

    private EduCourse requireTeacherCourse(Long teacherId, Long courseId) {
        EduCourse course = requireCourse(courseId);
        if (!teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权操作该课程");
        }
        return course;
    }

    private EduCourseChapter requireChapter(Long courseId, Long chapterId) {
        EduCourseChapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null || !courseId.equals(chapter.getCourseId())) {
            throw new IllegalArgumentException("章节不存在");
        }
        return chapter;
    }

    private EduCourseLesson requireLesson(Long courseId, Long lessonId) {
        EduCourseLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !courseId.equals(lesson.getCourseId())) {
            throw new IllegalArgumentException("课时不存在");
        }
        return lesson;
    }

    private void refreshTotalLessons(Long courseId) {
        List<EduCourseLesson> lessons = lessonMapper.selectList(Wrappers.<EduCourseLesson>lambdaQuery().eq(EduCourseLesson::getCourseId, courseId));
        EduCourse course = courseMapper.selectById(courseId);
        if (course != null) {
            course.setTotalLessons(lessons.size());
            course.setRequiredHours(calculateRequiredHours(lessons));
            courseMapper.updateById(course);
        }
    }

    private Map<Long, BigDecimal> getRequiredHoursMap(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return lessonMapper.selectList(new LambdaQueryWrapper<EduCourseLesson>()
                        .in(EduCourseLesson::getCourseId, courseIds)
                        .eq(EduCourseLesson::getStatus, 1))
                .stream()
                .collect(Collectors.groupingBy(EduCourseLesson::getCourseId,
                        Collectors.collectingAndThen(Collectors.toList(), this::calculateRequiredHours)));
    }

    private BigDecimal resolveRequiredHours(Long courseId, Map<Long, BigDecimal> requiredHoursMap) {
        return requiredHoursMap.getOrDefault(courseId, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal calculateRequiredHours(List<EduCourseLesson> lessons) {
        int totalSeconds = lessons == null ? 0 : lessons.stream()
                .filter(item -> item.getStatus() == null || item.getStatus() == 1)
                .map(EduCourseLesson::getDurationSeconds)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        return BigDecimal.valueOf(totalSeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int defaultBinary(Integer value) {
        return value == null ? 0 : (value == 0 ? 0 : 1);
    }
}
