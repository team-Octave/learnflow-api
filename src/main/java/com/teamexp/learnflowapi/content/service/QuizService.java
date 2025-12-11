package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.QuizRequest;
import com.teamexp.learnflowapi.content.dto.QuizResponse;
import com.teamexp.learnflowapi.content.model.Quiz;
import com.teamexp.learnflowapi.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    // 레슨 별 퀴즈 조회
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByLesson(Long lessonId) {
        List<Quiz> quizzes = quizRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
        return quizzes.stream()
                .map(QuizResponse::from)
                .toList();
    }

    // 퀴즈 생성
    @Transactional
    public QuizResponse createQuiz(Long lessonId, QuizRequest request) {

        Quiz quiz = Quiz.createQuiz(lessonId, request.orderIndex(), request.question(), request.correct());

        Quiz saved = quizRepository.save(quiz);
        return QuizResponse.from(saved);
    }

    // 코드 수정 변경 에정
//    // 퀴즈 저장
//    @Transactional
//    public QuizListResponse saveQuizzes(Long lessonId, QuizSaveRequest request) {
//
//        // 기존 퀴즈 삭제
//        quizRepository.deleteByLessonId(lessonId);
//
//        int orderIndex = 0;
//        List<Quiz> saved = new ArrayList<>();
//
//        for (QuizItemRequest item : request.quizzes()) {
//            Quiz quiz = Quiz.createQuiz(
//                    lessonId,
//                    orderIndex++,
//                    item.question(),
//                    item.correct()
//            );
//            saved.add(quizRepository.save(quiz));
//        }
//
//        return QuizListResponse.of(saved);
//    }

}
