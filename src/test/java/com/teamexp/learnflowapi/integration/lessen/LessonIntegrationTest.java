package com.teamexp.learnflowapi.integration.lessen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestMockConfig.class)
public class LessonIntegrationTest {

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
    ChapterRepository chapterRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    ContentMediaRepository contentMediaRepository;

    @Autowired
    EntityManager em;

    private User instructor;
    private User student;
    private String accessToken;

    private Lecture lectureA;   // 수강 강의
    private Lecture lectureB;   // 미수강 강의

    private Lesson lessonA1;
    private Lesson lessonB1;

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

        lectureA = Lecture.createLecture(
                "lectureA",
                "descA",
                LectureLevel.BEGINNER,
                1,
                instructor.getUserId(),
                "http://example.com/thumbA.jpg",
                PaymentType.FREE
        );
        lectureA.allowPublish();
        lectureA = lectureRepository.saveAndFlush(lectureA);

        LectureStatistic statA = LectureStatistic.createInitial(lectureA);
        lectureStatisticRepository.saveAndFlush(statA);

        Chapter chapterA = Chapter.createChapter("chapterA", 1);
        ReflectionTestUtils.setField(chapterA, "lecture", lectureA);
        chapterA = chapterRepository.save(chapterA);

        lessonA1 = Lesson.createLesson(
                LessonType.VIDEO,
                "lessonA-1",
                1,
                false,
                "http://example.com/videoA1.mp4"
        );
        chapterA.addLesson(lessonA1);
        lessonA1 = lessonRepository.save(lessonA1);

        attachCompletedMediaToLesson(lessonA1.getId());

        enrollmentRepository.saveAndFlush(Enrollment.create(student.getUserId(), lectureA.getId()));

        lectureB = Lecture.createLecture(
                "lectureB",
                "descB",
                LectureLevel.BEGINNER,
                1,
                instructor.getUserId(),
                "http://example.com/thumbB.jpg"
                , PaymentType.FREE
        );
        lectureB.allowPublish();
        lectureB = lectureRepository.saveAndFlush(lectureB);

        LectureStatistic statB = LectureStatistic.createInitial(lectureB);
        lectureStatisticRepository.saveAndFlush(statB);

        Chapter chapterB = Chapter.createChapter("chapterB", 1);
        ReflectionTestUtils.setField(chapterB, "lecture", lectureB);
        chapterB = chapterRepository.save(chapterB);

        lessonB1 = Lesson.createLesson(
                LessonType.VIDEO,
                "lessonB-1",
                1,
                false,
                "http://example.com/videoB1.mp4"
        );
        chapterB.addLesson(lessonB1);
        lessonB1 = lessonRepository.save(lessonB1);

        em.flush();
        em.clear();
    }

    private void attachCompletedMediaToLesson(Long lessonId) {
        ContentMedia media = ContentMedia.createPending("test/file-key.mp4", "video.mp4");
        media.attachToLesson(lessonId);
        media.completeUpload(60);
        contentMediaRepository.saveAndFlush(media);
    }

    @Test
    @DisplayName("레슨 조회 성공")
    void tc12_get_lesson_success() throws Exception {
        mockMvc.perform(
                        get("/api/v2/lectures/{lectureId}/lessons/{lessonId}", lectureA.getId(), lessonA1.getId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("미수강 강의 접근 실패")
    void tc13_get_lesson_fail_when_not_enrolled() throws Exception {
        mockMvc.perform(
                        get("/api/v2/lectures/{lectureId}/lessons/{lessonId}", lectureB.getId(), lessonB1.getId())
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("존재하지 않는 레슨 조회")
    void tc14_get_lesson_fail_when_lesson_not_exists() throws Exception {
        mockMvc.perform(
                        get("/api/v2/lectures/{lectureId}/lessons/{lessonId}", lectureA.getId(), Long.MAX_VALUE)
                                .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isNotFound());
    }
}
