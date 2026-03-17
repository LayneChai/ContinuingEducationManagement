package com.qizhi.continueeducation.module.exam.service;

import com.qizhi.continueeducation.module.exam.dto.ExamQuestionSaveRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSaveRequest;
import com.qizhi.continueeducation.module.exam.dto.ExamSubmitRequest;
import com.qizhi.continueeducation.module.exam.vo.ExamDetailVo;
import com.qizhi.continueeducation.module.exam.vo.ExamListItemVo;
import com.qizhi.continueeducation.module.exam.vo.ExamQuestionVo;
import com.qizhi.continueeducation.module.exam.vo.ExamSubmitResultVo;

import java.util.List;

public interface ExamService {

    List<ExamQuestionVo> teacherQuestionList(Long teacherId, Long courseId);

    Long createQuestion(Long teacherId, ExamQuestionSaveRequest request);

    void updateQuestion(Long teacherId, Long questionId, ExamQuestionSaveRequest request);

    void deleteQuestion(Long teacherId, Long questionId);

    List<ExamListItemVo> teacherExamList(Long teacherId, Long courseId);

    Long createExam(Long teacherId, ExamSaveRequest request);

    void updateExam(Long teacherId, Long examId, ExamSaveRequest request);

    ExamDetailVo teacherExamDetail(Long teacherId, Long examId);

    void publishExam(Long teacherId, Long examId);

    List<ExamListItemVo> studentExamList(Long studentId, Long courseId);

    ExamDetailVo studentExamDetail(Long studentId, Long examId);

    ExamSubmitResultVo submitExam(Long studentId, Long examId, ExamSubmitRequest request);
}
