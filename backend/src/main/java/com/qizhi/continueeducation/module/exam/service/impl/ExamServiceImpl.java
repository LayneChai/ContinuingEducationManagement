package com.qizhi.continueeducation.module.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qizhi.continueeducation.module.course.entity.EduCourse;
import com.qizhi.continueeducation.module.course.entity.EduCourseEnrollment;
import com.qizhi.continueeducation.module.course.mapper.EduCourseEnrollmentMapper;
import com.qizhi.continueeducation.module.course.mapper.EduCourseMapper;
import com.qizhi.continueeducation.module.exam.dto.ExamQuestionOptionRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamQuestionSaveRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSaveRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSubmitAnswerRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSubmitRequest;
import com.qizhi.continueeducation.module.exam.entity.EduExam;
import com.qizhi.continueeducation.module.exam.entity.EduExamAnswer;
import com.qizhi.continueeducation.module.exam.entity.EduExamPaperQuestion;
import com.qizhi.continueeducation.module.exam.entity.EduExamQuestion;
import com.qizhi.continueeducation.module.exam.entity.EduExamQuestionOption;
import com.qizhi.continueeducation.module.exam.entity.EduExamRecord;
import com.qizhi.continueeducation.module.exam.mapper.EduExamAnswerMapper;
import com.qizhi.continueeducation.module.exam.mapper.EduExamMapper;
import com.qizhi.continueeducation.module.exam.mapper.EduExamPaperQuestionMapper;
import com.qizhi.continueeducation.module.exam.mapper.EduExamQuestionMapper;
import com.qizhi.continueeducation.module.exam.mapper.EduExamQuestionOptionMapper;
import com.qizhi.continueeducation.module.exam.mapper.EduExamRecordMapper;
import com.qizhi.continueeducation.module.exam.service.ExamService;
import com.qizhi.continueeducation.module.exam.vo.ExamDetailVo;
import com.qizhi.continueeducation.module.exam.vo.ExamListItemVo;
import com.qizhi.continueeducation.module.exam.vo.ExamQuestionOptionVo;
import com.qizhi.continueeducation.module.exam.vo.ExamQuestionVo;
import com.qizhi.continueeducation.module.exam.vo.ExamSubmitResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final EduCourseMapper courseMapper;
    private final EduCourseEnrollmentMapper enrollmentMapper;
    private final EduExamMapper examMapper;
    private final EduExamQuestionMapper questionMapper;
    private final EduExamQuestionOptionMapper optionMapper;
    private final EduExamPaperQuestionMapper paperQuestionMapper;
    private final EduExamRecordMapper recordMapper;
    private final EduExamAnswerMapper answerMapper;

    @Override
    public List<ExamQuestionVo> teacherQuestionList(Long teacherId, Long courseId) {
        requireTeacherCourse(teacherId, courseId);
        return toQuestionVos(questionMapper.selectList(new LambdaQueryWrapper<EduExamQuestion>()
                .eq(EduExamQuestion::getCourseId, courseId)
                .orderByDesc(EduExamQuestion::getId)), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(Long teacherId, ExamQuestionSaveRequest request) {
        requireTeacherCourse(teacherId, request.getCourseId());
        validateQuestion(request);
        EduExamQuestion question = new EduExamQuestion();
        fillQuestion(question, request);
        questionMapper.insert(question);
        saveOptions(question.getId(), request.getOptions());
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long teacherId, Long questionId, ExamQuestionSaveRequest request) {
        requireTeacherCourse(teacherId, request.getCourseId());
        EduExamQuestion question = requireQuestion(questionId);
        if (!request.getCourseId().equals(question.getCourseId())) {
            throw new IllegalArgumentException("题目与课程不匹配");
        }
        validateQuestion(request);
        fillQuestion(question, request);
        questionMapper.updateById(question);
        optionMapper.delete(Wrappers.<EduExamQuestionOption>lambdaQuery().eq(EduExamQuestionOption::getQuestionId, questionId));
        saveOptions(questionId, request.getOptions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long teacherId, Long questionId) {
        EduExamQuestion question = requireQuestion(questionId);
        requireTeacherCourse(teacherId, question.getCourseId());
        long usage = paperQuestionMapper.selectCount(new LambdaQueryWrapper<EduExamPaperQuestion>().eq(EduExamPaperQuestion::getQuestionId, questionId));
        if (usage > 0) {
            throw new IllegalStateException("题目已被考试引用，无法删除");
        }
        optionMapper.delete(Wrappers.<EduExamQuestionOption>lambdaQuery().eq(EduExamQuestionOption::getQuestionId, questionId));
        questionMapper.deleteById(questionId);
    }

    @Override
    public List<ExamListItemVo> teacherExamList(Long teacherId, Long courseId) {
        if (courseId != null) {
            requireTeacherCourse(teacherId, courseId);
        }
        List<Long> allowedCourseIds = courseId == null ? teacherCourseIds(teacherId) : List.of(courseId);
        if (allowedCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<EduExam> exams = examMapper.selectList(new LambdaQueryWrapper<EduExam>()
                .in(EduExam::getCourseId, allowedCourseIds)
                .orderByDesc(EduExam::getCreateTime));
        return toExamListVos(exams, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExam(Long teacherId, ExamSaveRequest request) {
        EduCourse course = requireTeacherCourse(teacherId, request.getCourseId());
        List<EduExamQuestion> questions = requireQuestions(request.getQuestionIds(), request.getCourseId());
        EduExam exam = new EduExam();
        fillExam(exam, request, questions);
        exam.setStatus(0);
        examMapper.insert(exam);
        savePaperQuestions(exam.getId(), questions);
        course.setExamRequired(1);
        courseMapper.updateById(course);
        return exam.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExam(Long teacherId, Long examId, ExamSaveRequest request) {
        EduExam exam = requireExam(examId);
        requireTeacherCourse(teacherId, exam.getCourseId());
        if (!exam.getCourseId().equals(request.getCourseId())) {
            throw new IllegalArgumentException("不允许修改考试所属课程");
        }
        List<EduExamQuestion> questions = requireQuestions(request.getQuestionIds(), request.getCourseId());
        fillExam(exam, request, questions);
        exam.setStatus(0);
        examMapper.updateById(exam);
        paperQuestionMapper.delete(Wrappers.<EduExamPaperQuestion>lambdaQuery().eq(EduExamPaperQuestion::getExamId, examId));
        savePaperQuestions(examId, questions);
    }

    @Override
    public ExamDetailVo teacherExamDetail(Long teacherId, Long examId) {
        EduExam exam = requireExam(examId);
        requireTeacherCourse(teacherId, exam.getCourseId());
        return toExamDetailVo(exam, true, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishExam(Long teacherId, Long examId) {
        EduExam exam = requireExam(examId);
        requireTeacherCourse(teacherId, exam.getCourseId());
        if (paperQuestionMapper.selectCount(new LambdaQueryWrapper<EduExamPaperQuestion>().eq(EduExamPaperQuestion::getExamId, examId)) == 0) {
            throw new IllegalStateException("考试未配置题目");
        }
        exam.setStatus(1);
        examMapper.updateById(exam);
    }

    @Override
    public List<ExamListItemVo> studentExamList(Long studentId, Long courseId) {
        List<Long> enrolledCourseIds = enrolledCourseIds(studentId, courseId);
        if (enrolledCourseIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<EduExam> exams = examMapper.selectList(new LambdaQueryWrapper<EduExam>()
                .in(EduExam::getCourseId, enrolledCourseIds)
                .eq(EduExam::getStatus, 1)
                .orderByDesc(EduExam::getStartTime)
                .orderByDesc(EduExam::getCreateTime));
        return toExamListVos(exams, studentId);
    }

    @Override
    public ExamDetailVo studentExamDetail(Long studentId, Long examId) {
        EduExam exam = requireExam(examId);
        requireStudentExamAccess(studentId, exam);
        return toExamDetailVo(exam, false, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamSubmitResultVo submitExam(Long studentId, Long examId, ExamSubmitRequest request) {
        EduExam exam = requireExam(examId);
        requireStudentExamAccess(studentId, exam);
        int attemptNo = recordMapper.selectCount(new LambdaQueryWrapper<EduExamRecord>()
                .eq(EduExamRecord::getExamId, examId)
                .eq(EduExamRecord::getStudentId, studentId)).intValue() + 1;
        if (attemptNo > exam.getAttemptLimit()) {
            throw new IllegalStateException("考试次数已用完");
        }

        List<EduExamQuestion> questions = examQuestions(examId);
        Map<Long, EduExamPaperQuestion> paperMap = paperQuestionMapper.selectList(new LambdaQueryWrapper<EduExamPaperQuestion>()
                        .eq(EduExamPaperQuestion::getExamId, examId))
                .stream().collect(Collectors.toMap(EduExamPaperQuestion::getQuestionId, Function.identity(), (a, b) -> a));
        Map<Long, String> answerMap = request.getAnswers().stream()
                .collect(Collectors.toMap(ExamSubmitAnswerRequest::getQuestionId, item -> item.getAnswer() == null ? "" : item.getAnswer().trim(), (a, b) -> b, LinkedHashMap::new));

        EduExamRecord record = new EduExamRecord();
        record.setExamId(examId);
        record.setCourseId(exam.getCourseId());
        record.setStudentId(studentId);
        record.setAttemptNo(attemptNo);
        record.setStartTime(LocalDateTime.now());
        record.setSubmitTime(LocalDateTime.now());

        BigDecimal objectiveScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        int pendingReviewCount = 0;
        for (EduExamQuestion question : questions) {
            String studentAnswer = answerMap.getOrDefault(question.getId(), "");
            EduExamAnswer answer = new EduExamAnswer();
            answer.setExamId(examId);
            answer.setQuestionId(question.getId());
            answer.setStudentId(studentId);
            answer.setStudentAnswer(studentAnswer);
            BigDecimal itemScore = paperMap.get(question.getId()).getScore();
            if (question.getQuestionType() == 4) {
                answer.setIsCorrect(null);
                answer.setScore(BigDecimal.ZERO);
                pendingReviewCount++;
            } else {
                boolean correct = normalizeAnswer(question.getCorrectAnswer()).equals(normalizeAnswer(studentAnswer));
                answer.setIsCorrect(correct ? 1 : 0);
                answer.setScore(correct ? itemScore : BigDecimal.ZERO);
                objectiveScore = objectiveScore.add(answer.getScore());
            }
            if (question.getQuestionType() == 4) {
                subjectiveScore = subjectiveScore.add(BigDecimal.ZERO);
            }
            answer.setAiScore(null);
            answer.setAiComment(null);
            answer.setReviewComment(null);
            record.setObjectiveScore(objectiveScore);
            record.setSubjectiveScore(subjectiveScore);
        }

        BigDecimal totalScore = objectiveScore.add(subjectiveScore);
        record.setTotalScore(totalScore);
        record.setIsPass(totalScore.compareTo(exam.getPassScore()) >= 0 && pendingReviewCount == 0 ? 1 : 0);
        record.setStatus(pendingReviewCount == 0 ? 3 : 2);
        if (pendingReviewCount == 0) {
            record.setReviewTime(LocalDateTime.now());
        }
        recordMapper.insert(record);

        for (EduExamQuestion question : questions) {
            String studentAnswer = answerMap.getOrDefault(question.getId(), "");
            EduExamAnswer answer = new EduExamAnswer();
            answer.setRecordId(record.getId());
            answer.setExamId(examId);
            answer.setQuestionId(question.getId());
            answer.setStudentId(studentId);
            answer.setStudentAnswer(studentAnswer);
            BigDecimal itemScore = paperMap.get(question.getId()).getScore();
            if (question.getQuestionType() == 4) {
                answer.setIsCorrect(null);
                answer.setScore(BigDecimal.ZERO);
            } else {
                boolean correct = normalizeAnswer(question.getCorrectAnswer()).equals(normalizeAnswer(studentAnswer));
                answer.setIsCorrect(correct ? 1 : 0);
                answer.setScore(correct ? itemScore : BigDecimal.ZERO);
            }
            answerMapper.insert(answer);
        }

        return ExamSubmitResultVo.builder()
                .recordId(record.getId())
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(record.getSubjectiveScore())
                .totalScore(record.getTotalScore())
                .passed(record.getIsPass())
                .pendingReviewCount(pendingReviewCount)
                .build();
    }

    private void validateQuestion(ExamQuestionSaveRequest request) {
        if (request.getQuestionType() != 4 && (request.getOptions() == null || request.getOptions().isEmpty())) {
            throw new IllegalArgumentException("客观题必须配置选项");
        }
        if (request.getQuestionType() == 4 && (request.getCorrectAnswer() == null || request.getCorrectAnswer().isBlank())) {
            request.setCorrectAnswer("");
        }
    }

    private void fillQuestion(EduExamQuestion question, ExamQuestionSaveRequest request) {
        question.setCourseId(request.getCourseId());
        question.setQuestionType(request.getQuestionType());
        question.setStem(request.getStem());
        question.setAnalysis(request.getAnalysis());
        question.setDifficulty(request.getDifficulty() == null ? 1 : request.getDifficulty());
        question.setScore(request.getScore());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private void saveOptions(Long questionId, List<ExamQuestionOptionRequest> options) {
        if (options == null) {
            return;
        }
        for (ExamQuestionOptionRequest item : options) {
            EduExamQuestionOption option = new EduExamQuestionOption();
            option.setQuestionId(questionId);
            option.setOptionLabel(item.getOptionLabel());
            option.setOptionContent(item.getOptionContent());
            option.setIsCorrect(item.getIsCorrect() == null ? 0 : item.getIsCorrect());
            option.setSort(item.getSort() == null ? 0 : item.getSort());
            optionMapper.insert(option);
        }
    }

    private void fillExam(EduExam exam, ExamSaveRequest request, List<EduExamQuestion> questions) {
        BigDecimal totalScore = questions.stream().map(EduExamQuestion::getScore).reduce(BigDecimal.ZERO, BigDecimal::add);
        exam.setCourseId(request.getCourseId());
        exam.setTitle(request.getTitle());
        exam.setDescription(request.getDescription());
        exam.setDurationMinutes(request.getDurationMinutes() == null ? 60 : request.getDurationMinutes());
        exam.setPassScore(request.getPassScore() == null ? BigDecimal.valueOf(60) : request.getPassScore());
        exam.setAttemptLimit(request.getAttemptLimit() == null ? 1 : request.getAttemptLimit());
        exam.setQuestionCount(questions.size());
        exam.setTotalScore(totalScore);
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
    }

    private void savePaperQuestions(Long examId, List<EduExamQuestion> questions) {
        int sort = 1;
        for (EduExamQuestion question : questions) {
            EduExamPaperQuestion item = new EduExamPaperQuestion();
            item.setExamId(examId);
            item.setQuestionId(question.getId());
            item.setScore(question.getScore());
            item.setSort(sort++);
            paperQuestionMapper.insert(item);
        }
    }

    private List<EduExamQuestion> requireQuestions(List<Long> questionIds, Long courseId) {
        List<EduExamQuestion> questions = questionMapper.selectBatchIds(questionIds);
        if (questions.size() != questionIds.size()) {
            throw new IllegalArgumentException("存在无效题目");
        }
        boolean invalid = questions.stream().anyMatch(item -> !courseId.equals(item.getCourseId()));
        if (invalid) {
            throw new IllegalArgumentException("题目不属于当前课程");
        }
        return questions.stream().sorted(Comparator.comparingInt(item -> questionIds.indexOf(item.getId()))).toList();
    }

    private List<ExamQuestionVo> toQuestionVos(List<EduExamQuestion> questions, boolean includeAnswer) {
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }
        List<EduExamQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EduExamQuestionOption>()
                .in(EduExamQuestionOption::getQuestionId, questions.stream().map(EduExamQuestion::getId).toList())
                .orderByAsc(EduExamQuestionOption::getSort)
                .orderByAsc(EduExamQuestionOption::getId));
        Map<Long, List<ExamQuestionOptionVo>> optionMap = options.stream().collect(Collectors.groupingBy(EduExamQuestionOption::getQuestionId,
                Collectors.mapping(item -> ExamQuestionOptionVo.builder()
                        .id(item.getId())
                        .optionLabel(item.getOptionLabel())
                        .optionContent(item.getOptionContent())
                        .sort(item.getSort())
                        .build(), Collectors.toList())));
        return questions.stream().map(item -> ExamQuestionVo.builder()
                .id(item.getId())
                .courseId(item.getCourseId())
                .questionType(item.getQuestionType())
                .stem(item.getStem())
                .analysis(includeAnswer ? item.getAnalysis() : null)
                .difficulty(item.getDifficulty())
                .score(item.getScore())
                .correctAnswer(includeAnswer ? item.getCorrectAnswer() : null)
                .status(item.getStatus())
                .options(optionMap.getOrDefault(item.getId(), List.of()))
                .build()).toList();
    }

    private List<ExamListItemVo> toExamListVos(List<EduExam> exams, Long studentId) {
        if (exams.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, EduCourse> courseMap = courseMapper.selectBatchIds(exams.stream().map(EduExam::getCourseId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(EduCourse::getId, Function.identity(), (a, b) -> a));
        Map<Long, List<EduExamRecord>> recordMap = studentId == null ? Collections.emptyMap() : recordMapper.selectList(new LambdaQueryWrapper<EduExamRecord>()
                        .eq(EduExamRecord::getStudentId, studentId)
                        .in(EduExamRecord::getExamId, exams.stream().map(EduExam::getId).toList())
                        .orderByDesc(EduExamRecord::getAttemptNo))
                .stream().collect(Collectors.groupingBy(EduExamRecord::getExamId));
        return exams.stream().map(item -> {
            List<EduExamRecord> records = recordMap.getOrDefault(item.getId(), List.of());
            EduExamRecord latest = records.isEmpty() ? null : records.get(0);
            EduCourse course = courseMap.get(item.getCourseId());
            return ExamListItemVo.builder()
                    .id(item.getId())
                    .courseId(item.getCourseId())
                    .courseTitle(course == null ? null : course.getTitle())
                    .title(item.getTitle())
                    .durationMinutes(item.getDurationMinutes())
                    .totalScore(item.getTotalScore())
                    .passScore(item.getPassScore())
                    .attemptLimit(item.getAttemptLimit())
                    .questionCount(item.getQuestionCount())
                    .startTime(item.getStartTime())
                    .endTime(item.getEndTime())
                    .status(item.getStatus())
                    .myAttemptCount(records.size())
                    .latestScore(latest == null ? null : latest.getTotalScore())
                    .latestPass(latest == null ? null : latest.getIsPass())
                    .build();
        }).toList();
    }

    private ExamDetailVo toExamDetailVo(EduExam exam, boolean includeAnswer, Long studentId) {
        EduCourse course = courseMapper.selectById(exam.getCourseId());
        List<EduExamPaperQuestion> paperQuestions = paperQuestionMapper.selectList(new LambdaQueryWrapper<EduExamPaperQuestion>()
                .eq(EduExamPaperQuestion::getExamId, exam.getId())
                .orderByAsc(EduExamPaperQuestion::getSort)
                .orderByAsc(EduExamPaperQuestion::getId));
        List<EduExamQuestion> questions = questionMapper.selectBatchIds(paperQuestions.stream().map(EduExamPaperQuestion::getQuestionId).toList());
        Map<Long, EduExamQuestion> questionMap = questions.stream().collect(Collectors.toMap(EduExamQuestion::getId, Function.identity(), (a, b) -> a));
        List<ExamQuestionVo> questionVos = toQuestionVos(paperQuestions.stream().map(item -> questionMap.get(item.getQuestionId())).toList(), includeAnswer);
        int attemptCount = studentId == null ? 0 : recordMapper.selectCount(new LambdaQueryWrapper<EduExamRecord>()
                .eq(EduExamRecord::getExamId, exam.getId())
                .eq(EduExamRecord::getStudentId, studentId)).intValue();
        return ExamDetailVo.builder()
                .id(exam.getId())
                .courseId(exam.getCourseId())
                .courseTitle(course == null ? null : course.getTitle())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .durationMinutes(exam.getDurationMinutes())
                .totalScore(exam.getTotalScore())
                .passScore(exam.getPassScore())
                .attemptLimit(exam.getAttemptLimit())
                .questionCount(exam.getQuestionCount())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus())
                .myAttemptCount(attemptCount)
                .questions(questionVos)
                .build();
    }

    private EduCourse requireTeacherCourse(Long teacherId, Long courseId) {
        EduCourse course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (!teacherId.equals(course.getTeacherId())) {
            throw new IllegalArgumentException("无权操作该课程考试");
        }
        return course;
    }

    private EduExam requireExam(Long examId) {
        EduExam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new IllegalArgumentException("考试不存在");
        }
        return exam;
    }

    private EduExamQuestion requireQuestion(Long questionId) {
        EduExamQuestion question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        return question;
    }

    private void requireStudentExamAccess(Long studentId, EduExam exam) {
        if (exam.getStatus() != 1) {
            throw new IllegalStateException("考试未发布");
        }
        boolean enrolled = enrollmentMapper.selectCount(new LambdaQueryWrapper<EduCourseEnrollment>()
                .eq(EduCourseEnrollment::getStudentId, studentId)
                .eq(EduCourseEnrollment::getCourseId, exam.getCourseId())) > 0;
        if (!enrolled) {
            throw new IllegalStateException("请先报名课程");
        }
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new IllegalStateException("考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new IllegalStateException("考试已结束");
        }
    }

    private List<Long> teacherCourseIds(Long teacherId) {
        return courseMapper.selectList(new LambdaQueryWrapper<EduCourse>()
                        .select(EduCourse::getId)
                        .eq(EduCourse::getTeacherId, teacherId))
                .stream().map(EduCourse::getId).toList();
    }

    private List<Long> enrolledCourseIds(Long studentId, Long courseId) {
        return enrollmentMapper.selectList(new LambdaQueryWrapper<EduCourseEnrollment>()
                        .select(EduCourseEnrollment::getCourseId)
                        .eq(EduCourseEnrollment::getStudentId, studentId)
                        .eq(courseId != null, EduCourseEnrollment::getCourseId, courseId))
                .stream().map(EduCourseEnrollment::getCourseId).toList();
    }

    private List<EduExamQuestion> examQuestions(Long examId) {
        List<EduExamPaperQuestion> paperQuestions = paperQuestionMapper.selectList(new LambdaQueryWrapper<EduExamPaperQuestion>()
                .eq(EduExamPaperQuestion::getExamId, examId)
                .orderByAsc(EduExamPaperQuestion::getSort)
                .orderByAsc(EduExamPaperQuestion::getId));
        if (paperQuestions.isEmpty()) {
            return List.of();
        }
        Map<Long, EduExamQuestion> map = questionMapper.selectBatchIds(paperQuestions.stream().map(EduExamPaperQuestion::getQuestionId).toList())
                .stream().collect(Collectors.toMap(EduExamQuestion::getId, Function.identity(), (a, b) -> a));
        return paperQuestions.stream().map(item -> map.get(item.getQuestionId())).toList();
    }

    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String[] parts = answer.replace("，", ",").replace(" ", "").toUpperCase().split(",");
        Set<String> set = java.util.Arrays.stream(parts).filter(item -> !item.isBlank()).collect(Collectors.toCollection(java.util.TreeSet::new));
        return String.join(",", set);
    }
}
