package com.teamexp.learnflowapi.integration.enrollment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.enrollment.model.Enrollment;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import com.teamexp.learnflowapi.lecture.model.PaymentType;
import com.teamexp.learnflowapi.lecture.repository.ChapterRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestMockConfig.class)
public class EnrollmentIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LectureRepository lectureRepository;

    @Autowired
    LectureStatisticRepository lectureStatisticRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    ChapterRepository chapterRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    EntityManager em;


    private User instructor;
    private User student;
    private String accessToken;
    private Lecture availableLecture;

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
    }

    @Disabled("cd테스트 에러")
    @Test
    @DisplayName("수강신청 성공")
    void tc8_enrollment_success() throws Exception {
        String body = """
                {
                  "lectureId": %d
                }
                """.formatted(availableLecture.getId());

        mockMvc.perform(
                        post("/api/v1/enrollment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + accessToken)
                                .content(body)
                )
                .andExpect(status().isCreated());
    }

    @Disabled("cd테스트 에러")
    @Test
    @DisplayName("중복 수강신청 실패")
    void tc9_enrollment_duplicate_fail() throws Exception {
        String body = """
            {
              "lectureId": %d
            }
            """.formatted(availableLecture.getId());

        mockMvc.perform(
                        post("/api/v1/enrollment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + accessToken)
                                .content(body)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/enrollment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + accessToken)
                                .content(body)
                )
                .andExpect(status().isConflict());
    }

    @Disabled("cd테스트 에러")
    @Test
    @DisplayName("수강신청 취소 성공")
    void tc10_enrollment_cancel_success() throws Exception {
        String createBody = """
        {
          "lectureId": %d
        }
        """.formatted(availableLecture.getId());

        mockMvc.perform(
                post("/api/v1/enrollment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(createBody)
        ).andExpect(status().isCreated());

        var enrollment = enrollmentRepository
                .findByUserIdAndLectureId(student.getUserId(), availableLecture.getId())
                .orElseThrow();

        String deleteBody = """
        {
          "enrollmentId": %d
        }
        """.formatted(enrollment.getId());

        mockMvc.perform(
                delete("/api/v1/enrollment/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(deleteBody)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("tc11. 강의 등록자 본인의 강의 수강 신청 실패")
    void tc11_instructor_enrollment_fail() throws Exception {
        String instructorToken = jwtTokenProvider.createAccessToken(
                instructor.getUserId(),
                instructor.getEmail(),
                instructor.getRole().name(),
                instructor.getNickname()
        );

        String body = """
            {
              "lectureId": %d
            }
            """.formatted(availableLecture.getId());

        mockMvc.perform(
                        post("/api/v1/enrollment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + instructorToken)
                                .content(body)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Disabled("cd테스트 에러")
    @Test
    @DisplayName("레슨완료")
    void tc15_complete_lesson_success() throws Exception {
        Chapter chapter = Chapter.createChapter("test chapter", 1);
        ReflectionTestUtils.setField(chapter, "lecture", availableLecture);
        chapterRepository.save(chapter);

        Lesson lesson = Lesson.createLesson(
                LessonType.VIDEO,
                "lesson-1",
                1,
                false,
                "http://example.com/video.mp4"
        );
        chapter.addLesson(lesson);
        lessonRepository.save(lesson);

        em.clear();

        mockMvc.perform(post("/api/v1/enrollment")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "lectureId": %d }
                    """.formatted(availableLecture.getId())))
                .andExpect(status().isCreated());

        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndLectureId(student.getUserId(), availableLecture.getId())
                .orElseThrow();

        mockMvc.perform(post("/api/v1/enrollment/complete-lesson")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "enrollmentId": %d, "lessonId": %d }
                    """.formatted(enrollment.getId(), lesson.getId())))
                .andExpect(status().isCreated());
    }

    @Disabled("cd테스트 에러")
    @Test
    @DisplayName("레슨완료 중복체크")
    void tc16_complete_lesson_duplicate_fail() throws Exception {
        Chapter chapter = Chapter.createChapter("test chapter", 1);
        ReflectionTestUtils.setField(chapter, "lecture", availableLecture);
        chapterRepository.save(chapter);

        Lesson lesson = Lesson.createLesson(
                LessonType.VIDEO,
                "lesson-1",
                1,
                false,
                "http://example.com/video.mp4"
        );
        chapter.addLesson(lesson);
        lessonRepository.save(lesson);

        em.clear();

        mockMvc.perform(post("/api/v1/enrollment")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "lectureId": %d }
                    """.formatted(availableLecture.getId())))
                .andExpect(status().isCreated());

        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndLectureId(student.getUserId(), availableLecture.getId())
                .orElseThrow();

        mockMvc.perform(post("/api/v1/enrollment/complete-lesson")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "enrollmentId": %d, "lessonId": %d }
                    """.formatted(enrollment.getId(), lesson.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/enrollment/complete-lesson")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "enrollmentId": %d, "lessonId": %d }
                    """.formatted(enrollment.getId(), lesson.getId())))
                .andExpect(status().isConflict());
    }

}
