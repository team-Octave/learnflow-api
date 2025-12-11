package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.QuizItemRequest;
import com.teamexp.learnflowapi.content.dto.QuizListResponse;
import com.teamexp.learnflowapi.content.dto.QuizSaveRequest;
import com.teamexp.learnflowapi.content.model.Quiz;
import com.teamexp.learnflowapi.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // 레슨 별 퀴즈 조회
    @Transactional(readOnly = true)
    public QuizListResponse getQuizzesByLesson(Long lessonId) {
        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return QuizListResponse.of(lessonId, quizzes);
    }

    @Transactional(readOnly = true)
    public List<Quiz> getQuizzes(Long lessonId) {
        return quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
    }

    // 퀴즈 생성
    @Transactional
    public Quiz createQuiz(Long lessonId, String question, Boolean correct, Integer orderIndex) {

        Quiz quiz = Quiz.createQuiz(lessonId, orderIndex, question, correct);
        return quizRepository.save(quiz);
    }

    // 퀴즈 저장
    @Transactional
    public QuizListResponse saveQuizzes(Long lessonId, QuizSaveRequest request) {

        // 기존 퀴즈 삭제
        quizRepository.deleteByLessonId(lessonId);

        int orderIndex = 0;
        List<Quiz> saved = new ArrayList<>();

        for (QuizItemRequest item : request.quizzes()) {
            Quiz quiz = Quiz.createQuiz(
                    lessonId,
                    orderIndex++,
                    item.question(),
                    item.correct()
            );
            saved.add(quizRepository.save(quiz));
        }

        return QuizListResponse.of(lessonId, saved);
    }

}
