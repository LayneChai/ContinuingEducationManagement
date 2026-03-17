package com.qizhi.continueeducation.module.learning.service;

import com.qizhi.continueeducation.module.learning.dto.LessonStudyRequest;
import com.qizhi.continueeducation.module.learning.vo.LearningOverviewVo;

import java.math.BigDecimal;
import java.util.Map;

public interface LearningService {

    void studyLesson(Long studentId, Long courseId, Long lessonId, LessonStudyRequest request);

    LearningOverviewVo getCourseLearningOverview(Long studentId, Long courseId);

    Map<Long, BigDecimal> getLessonProgressMap(Long studentId, Long courseId);
}
