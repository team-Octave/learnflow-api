package com.teamexp.learnflowapi.content.service;

import com.teamexp.learnflowapi.content.dto.QuizRequest;
import com.teamexp.learnflowapi.content.dto.QuizResponse;
import com.teamexp.learnflowapi.content.dto.QuizSaveRequest;
import com.teamexp.learnflowapi.content.dto.QuizUpdateListRequest;
import com.teamexp.learnflowapi.content.dto.QuizUpdateRequest;
import com.teamexp.learnflowapi.content.exception.QuizLessonMismatchException;
import com.teamexp.learnflowapi.content.exception.QuizNotFoundException;
import com.teamexp.learnflowapi.content.model.Quiz;
import com.teamexp.learnflowapi.content.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public List<QuizResponse> createQuiz(Long lessonId, QuizSaveRequest request) {

        List<Quiz> quizzes = request.quizzes().stream()
                .map(rq -> Quiz.createQuiz(
                        lessonId,
                        rq.orderIndex(),
                        rq.question(),
                        rq.correct()
                ))
                .toList();

        List<Quiz> savedQuizzes = quizRepository.saveAll(quizzes);

        return savedQuizzes.stream()
                .map(QuizResponse::from)
                .toList();
    }

    @Transactional
    public List<QuizResponse> updateQuizzes(Long lessonId, QuizUpdateListRequest request) {
        List<Quiz> quizzes = saveOrUpdateQuizzes(lessonId, request);
        return quizzes.stream()
                .map(QuizResponse::from)
                .toList();
    }

    // 퀴즈 수정(업데이트)
    @Transactional
    public List<Quiz> saveOrUpdateQuizzes(Long lessonId, QuizUpdateListRequest request) {

        // 요청에 들어온 퀴즈 ID 추출
        List<Long> quizIds = request.quizzes().stream()
                .map(QuizUpdateRequest::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 기존 퀴즈 조회
        List<Quiz> existingQuizzes = quizRepository.findAllById(quizIds);

        // 요청에 들어오지않은 기존의 퀴즈 목록을 삭제
        // quizIds - existingQuizzes.stream().map(Quiz::getId).toList()
        List<Long> disjointQuizIds = quizIds.stream()
                .filter(id -> !existingQuizzes.stream().map(Quiz::getId).toList().contains(id))
                .toList();
        
        quizRepository.deleteAllById(disjointQuizIds);

        // id를 엔티티로 변환
        Map<Long, Quiz> quizById = existingQuizzes.stream()
                .collect(Collectors.toMap(Quiz::getId, q -> q));

        List<Quiz> result = new ArrayList<>();

        // 요청 들어온 퀴즈들에 대해서만 수정 차리
        for (QuizUpdateRequest item : request.quizzes()) {

            // id가 null이면 새로 생성
            if (item.id() == null) {
                Quiz newQuiz = Quiz.createQuiz(
                        lessonId,
                        item.orderIndex(),
                        item.question(),
                        item.correct()
                );
                Quiz saved = quizRepository.save(newQuiz);
                result.add(saved);
                continue;
            }

            // id가 있으면 기존 퀴즈를 수정
            Quiz quiz = quizById.get(item.id());
            if (quiz == null) {
                throw new QuizNotFoundException();
            }
            if (!quiz.getLessonId().equals(lessonId)) {
                throw new QuizLessonMismatchException();
            }

            quiz.update(
                    item.orderIndex(),
                    item.question(),
                    item.correct()
            );
        }
        List<Quiz> savedQuizzes = quizRepository.saveAll(result);

        return savedQuizzes;
    }
}
