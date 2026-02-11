package com.teamexp.learnflowapi.integration.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.enrollment.model.CompletedLesson;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.CompletedLessonRepository;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import com.teamexp.learnflowapi.lecture.model.PaymentType;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import com.teamexp.learnflowapi.review.repository.ReviewRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestMockConfig.class)
public class ReviewIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    LectureRepository lectureRepository;

    @Autowired
    LectureStatisticRepository lectureStatisticRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    CompletedLessonRepository completedLessonRepository;

    private User instructor;
    private User student;
    private String accessToken;
    private Lecture availableLecture;
    private Enrollment enrollment;
    private Lesson lesson1, lesson2, lesson3;

    @BeforeEach
    void setUp() {
        instructor = userRepository.save(
                User.createUser("instructor@test.com", "password", "instructor", UserRole.MEMBER)
        );

        student = userRepository.save(
                User.createUser("student@test.com", "password", "student", UserRole.MEMBER)
        );

        accessToken = jwtTokenProvider.createAccessToken(
                student.getUserId(),
                student.getEmail(),
                student.getRole().name(),
                student.getNickname()
        );

        availableLecture = Lecture.createLecture(
                "test lecture",
                "test lecture description",
                LectureLevel.BEGINNER,
                1,
                instructor.getUserId(),
                "http://example.com/thumbnail.jpg",
                PaymentType.FREE
        );

        availableLecture.allowPublish();
        availableLecture = lectureRepository.saveAndFlush(availableLecture);

        LectureStatistic statistic = LectureStatistic.createInitial(availableLecture);
        lectureStatisticRepository.saveAndFlush(statistic);

        enrollment = enrollmentRepository.saveAndFlush(
                Enrollment.create(student.getUserId(), availableLecture.getId())
        );

        lesson1 = lessonRepository.save(Lesson.createLesson(LessonType.VIDEO, "L1", 1, false, "url1"));
        lesson2 = lessonRepository.save(Lesson.createLesson(LessonType.VIDEO, "L2", 2, false, "url2"));
        lesson3 = lessonRepository.save(Lesson.createLesson(LessonType.VIDEO, "L3", 3, false, "url3"));

        completedLessonRepository.saveAndFlush(CompletedLesson.createCompletedLesson(enrollment.getId(), lesson1.getId()));
        completedLessonRepository.saveAndFlush(CompletedLesson.createCompletedLesson(enrollment.getId(), lesson2.getId()));
        completedLessonRepository.saveAndFlush(CompletedLesson.createCompletedLesson(enrollment.getId(), lesson3.getId()));
    }

    @Test
    @DisplayName("리뷰 등록 성공")
    void tc_20_review_create_success() throws Exception {
        String body= """
                {
                	"lectureId": %d,
                	"rating": 5,
                	"content": "강의가 정말 재미 있네요"
                }
                """;

        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                        .content(body.formatted(availableLecture.getId())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void tc_21_review_delete_success() throws Exception {
        String body= """
                {
                	"lectureId": %d,
                	"rating": 5,
                	"content": "강의가 정말 재미 있네요"
                }
                """;

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(body.formatted(availableLecture.getId())))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository
                .findByEnrollment_IdIn(List.of(enrollment.getId()))
                .get(0)
                .getId();

        mockMvc.perform(delete("/api/v1/reviews/{reviewId}", reviewId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("강사 답글 등록 성공")
    void tc22_instructor_reply_create_success() throws Exception {
        String body= """
                {
                	"lectureId": %d,
                	"rating": 5,
                	"content": "강의가 정말 재미 있네요"
                }
                """;

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(body.formatted(availableLecture.getId())))
                .andExpect(status().isCreated());

        Long reviewId = reviewRepository
                .findByEnrollment_IdIn(List.of(enrollment.getId()))
                .get(0)
                .getId();

        String replyBody= """
                {
                	"replyContent": "소중한 리뷰 감사합니다!"
                }
                """;

        String instructorToken = jwtTokenProvider.createAccessToken(
                instructor.getUserId(),
                instructor.getEmail(),
                instructor.getRole().name(),
                instructor.getNickname()
        );

        mockMvc.perform(post("/api/v1/reviews/{reviewId}/reply", reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + instructorToken)
                        .content(replyBody))
                .andExpect(status().isCreated());
    }
}
