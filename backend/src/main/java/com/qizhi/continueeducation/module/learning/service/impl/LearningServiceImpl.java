package com.qizhi.continueeducation.module.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.entity.EduCourseLesson;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseLessonMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.learning.dto.LessonStudyRequest;
import com.qizhi.continueeducation.module.learning.entity.EduCourseHourStat;
import com.qizhi.continueeducation.module.learning.entity.EduLearningRecord;
import com.qizhi.continueeducation.module.learning.mapper.EduCourseHourStatMapper;
import com.qizhi.continueeducation.module.learning.mapper.EduLearningRecordMapper;
import com.qizhi.continueeducation.module.learning.service.LearningService;
import com.qizhi.continueeducation.module.learning.vo.LearningOverviewVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final EduCourseMapper courseMapper;
    private final EduCourseLessonMapper lessonMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final EduLearningRecordMapper learningRecordMapper;
    private final EduCourseHourStatMapper courseHourStatMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void studyLesson(Long studentId, Long courseId, Long lessonId, LessonStudyRequest request) {
        EduCourse course = requireCourse(courseId);
        EduCourseLesson lesson = requireLesson(courseId, lessonId);
        EduCourseEnrollment enrollment = requireEnrollment(studentId, courseId);

        EduLearningRecord record = learningRecordMapper.selectOne(new LambdaQueryWrapper<EduLearningRecord>()
                .eq(EduLearningRecord::getLessonId, lessonId)
                .eq(EduLearningRecord::getStudentId, studentId)
                .last("limit 1"));
        if (record == null) {
            record = new EduLearningRecord();
            record.setCourseId(courseId);
            record.setLessonId(lessonId);
            record.setStudentId(studentId);
            record.setFirstStudyTime(LocalDateTime.now());
        }

        record.setStudySeconds(Math.max(request.getStudySeconds(), 0));
        record.setProgressPercent(normalizePercent(request.getProgressPercent()));
        record.setLastPosition(Math.max(request.getLastPosition(), 0));
        boolean completed = (request.getCompleted() != null && request.getCompleted() == 1)
                || record.getProgressPercent().compareTo(BigDecimal.valueOf(100)) >= 0;
        record.setIsCompleted(completed ? 1 : 0);
        record.setLastStudyTime(LocalDateTime.now());

        if (record.getId() == null) {
            learningRecordMapper.insert(record);
        } else {
            learningRecordMapper.updateById(record);
        }

        refreshLearningStats(studentId, course, enrollment);
    }

    @Override
    public LearningOverviewVo getCourseLearningOverview(Long studentId, Long courseId) {
        requireEnrollment(studentId, courseId);
        EduCourse course = requireCourse(courseId);
        return buildOverview(studentId, course);
    }

    @Override
    public Map<Long, BigDecimal> getLessonProgressMap(Long studentId, Long courseId) {
        List<EduLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<EduLearningRecord>()
                .eq(EduLearningRecord::getStudentId, studentId)
                .eq(EduLearningRecord::getCourseId, courseId));
        if (records.isEmpty()) {
            return Collections.emptyMap();
        }
        return records.stream().collect(Collectors.toMap(EduLearningRecord::getLessonId, EduLearningRecord::getProgressPercent, (a, b) -> b));
    }

    private void refreshLearningStats(Long studentId, EduCourse course, EduCourseEnrollment enrollment) {
        LearningOverviewVo overview = buildOverview(studentId, course);

        EduCourseHourStat stat = courseHourStatMapper.selectOne(new LambdaQueryWrapper<EduCourseHourStat>()
                .eq(EduCourseHourStat::getCourseId, course.getId())
                .eq(EduCourseHourStat::getStudentId, studentId)
                .last("limit 1"));
        if (stat == null) {
            stat = new EduCourseHourStat();
            stat.setCourseId(course.getId());
            stat.setStudentId(studentId);
        }
        stat.setRequiredHours(overview.getRequiredHours());
        stat.setCompletedHours(overview.getCompletedHours());
        stat.setCompletionRate(overview.getCompletionRate());
        stat.setQualified(overview.getQualified());
        stat.setLastCalcTime(LocalDateTime.now());
        if (stat.getId() == null) {
            courseHourStatMapper.insert(stat);
        } else {
            courseHourStatMapper.updateById(stat);
        }

        enrollment.setProgressPercent(overview.getCompletionRate());
        if (overview.getCompletedLessons().equals(overview.getTotalLessons()) && overview.getTotalLessons() > 0) {
            enrollment.setStatus(2);
            enrollment.setCompletedTime(LocalDateTime.now());
        } else if (overview.getCompletedHours().compareTo(BigDecimal.ZERO) > 0) {
            enrollment.setStatus(1);
        }
        enrollmentMapper.updateById(enrollment);
    }

    private LearningOverviewVo buildOverview(Long studentId, EduCourse course) {
        List<EduCourseLesson> lessons = lessonMapper.selectList(new LambdaQueryWrapper<EduCourseLesson>()
                .eq(EduCourseLesson::getCourseId, course.getId())
                .eq(EduCourseLesson::getStatus, 1));
        List<EduLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<EduLearningRecord>()
                .eq(EduLearningRecord::getStudentId, studentId)
                .eq(EduLearningRecord::getCourseId, course.getId()));

        int totalLessons = lessons.size();
        int completedLessons = (int) records.stream().filter(item -> Integer.valueOf(1).equals(item.getIsCompleted())).count();
        int totalStudySeconds = records.stream().map(EduLearningRecord::getStudySeconds).filter(v -> v != null).mapToInt(Integer::intValue).sum();
        BigDecimal completedHours = BigDecimal.valueOf(totalStudySeconds)
                .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
        BigDecimal requiredHours = course.getRequiredHours() == null ? BigDecimal.ZERO : course.getRequiredHours();
        BigDecimal completionRate = requiredHours.compareTo(BigDecimal.ZERO) <= 0
                ? (totalLessons == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(completedLessons * 100.0 / totalLessons).setScale(2, RoundingMode.HALF_UP))
                : completedHours.multiply(BigDecimal.valueOf(100)).divide(requiredHours, 2, RoundingMode.HALF_UP).min(BigDecimal.valueOf(100));
        int qualified = requiredHours.compareTo(BigDecimal.ZERO) <= 0
                ? (totalLessons > 0 && completedLessons == totalLessons ? 1 : 0)
                : (completedHours.compareTo(requiredHours) >= 0 ? 1 : 0);

        return LearningOverviewVo.builder()
                .courseId(course.getId())
                .studentId(studentId)
                .requiredHours(requiredHours)
                .completedHours(completedHours)
                .completionRate(completionRate)
                .qualified(qualified)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .build();
    }

    private EduCourse requireCourse(Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        return course;
    }

    private EduCourseLesson requireLesson(Long courseId, Long lessonId) {
        EduCourseLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || !courseId.equals(lesson.getCourseId())) {
            throw new IllegalArgumentException("课时不存在");
        }
        return lesson;
    }

    private EduCourseEnrollment requireEnrollment(Long studentId, Long courseId) {
        EduCourseEnrollment enrollment = enrollmentMapper.selectOne(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .eq(EduCourseEnrollment::getCourseId, courseId)
                .last("limit 1"));
        if (enrollment == null || Integer.valueOf(3).equals(enrollment.getStatus())) {
            throw new IllegalStateException("请先报名课程");
        }
        return enrollment;
    }

    private BigDecimal normalizePercent(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
