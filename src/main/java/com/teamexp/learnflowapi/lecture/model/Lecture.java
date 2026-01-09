package com.teamexp.learnflowapi.lecture.model;

import com.teamexp.learnflowapi.lecture.exception.ChapterNotFoundException;
import com.teamexp.learnflowapi.lecture.exception.LectureAlreadyPublishedException;
import com.teamexp.learnflowapi.lecture.exception.LectureCannotPublishedWithoutChapterException;
import com.teamexp.learnflowapi.lecture.exception.LectureCannotPublishedWithoutLessonException;
import com.teamexp.learnflowapi.lecture.exception.LectureDeletedException;
import com.teamexp.learnflowapi.lecture.exception.LectureNotDeletedException;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "lectures")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NamedEntityGraph(
    name = "Lecture.withStatistic",
    attributeNodes = {
        @NamedAttributeNode("statistic")
    }
)
public class Lecture {

    /*
     * TODO(Work Plan)
     * 1) 상태 전이 규칙 확정:
     *    - UNAVAILABLE -> AVAILABLE (publish)
     *    - softDelete / restore 가능 조건
     *    - (선택) AVAILABLE -> UNAVAILABLE 허용 여부
     *
     * 2) 발행(AVAILABLE) 정책 분리 여부 결정:
     *    - 현재 validateForAvailable()의 "최소 1 챕터 + 최소 1 레슨" 규칙을
     *      LecturePublishPolicy(도메인 정책/서비스)로 이동할지 결정
     *    - 결정 전까지는 엔티티 내부 규칙으로 유지
     *
     * 3) 썸네일 모델 정리:
     *    - Thumbnail(Embedded) vs thumbnailId(FK) 중 하나로 통일
     *    - @Deprecated addThumbnailLink/setThumbnailId 제거 플랜에 맞춰 마이그레이션
     *
     * 4) 매핑/로딩 전략 재검토:
     *    - statistic(OneToOne) 매핑/EntityGraph가 의도대로 동작하는지 확인
     *    - 필요한 곳에만 Lazy 로딩 접근
     *
     * 5) 테스트(후속 작업):
     *    - publish 실패 케이스(챕터 없음/레슨 없음/이미 publish)
     *    - delete/restore 실패 케이스(이미 삭제/삭제 아님)
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // description have to allow setter? or make it immediately at creation? now make it at creation
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureLevel level;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column // 추후 nullablefalse로
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureStatus status;

    @Column(name="delete_flag", columnDefinition = "boolean default false") // 추후 nullable 다시 false로 
    private boolean deleteFlag;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "instructor_id") // user_id is UUID string
    private String instructorId;

    @Deprecated
    @Column(name = "thumbnail_id", insertable = false, updatable = false)
    private Long thumbnailId;
    
    @Column(name = "thumbnail_url", insertable = true, updatable = true) // thumbnail 역정규화
    private String thumbnailUrl;

    @OneToOne(mappedBy = "lecture", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private LectureStatistic statistic;

    public LectureStatistic getStatistic() {
        return statistic;
    }

//    // TODO: add price related VO and embed here
//    @Embedded
//    private Money price;

    // Aggregate root of Chapter & Lesson
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterOrder ASC")
    private Set<Chapter> chapters = new LinkedHashSet<>();

    protected Lecture() {}

    private Lecture(String title, String description, LectureLevel level, LectureStatus status, Integer categoryId, String instructorId, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.status = status;
        this.categoryId = categoryId;
        this.instructorId = instructorId;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Lecture createLecture(String title, String description, LectureLevel level, Integer categoryId, String instructorId, String thumbnailUrl) {
        return new Lecture(title, description, level, LectureStatus.UNAVAILABLE, categoryId, instructorId, thumbnailUrl);
    }


    // thumbnailId created after lecture creation, so need setter
    @Deprecated // 제거될 예정, UploadThumbnail -> createLecture 로 변환되면서 setter 제거 계획
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
        chapter.setLecture(this);
    }

    public Chapter findByChapterId(Long chapterId) {
        return chapters.stream()
            .filter(c -> c.getId().equals(chapterId))
            .findFirst()
            .orElseThrow(ChapterNotFoundException::new);
    }

    public void removeChapter(Long chapterId) {
        Chapter chapter = findByChapterId(chapterId);
        this.chapters.remove(chapter);
    }

    // After thumbnail is uploaded, call this method to link thumbnail to lecture
    @Deprecated // 제거될 예정, UploadThumbnail -> createLecture 로 변환되면서 setter 제거 계획
    public void addThumbnailLink(String thumbnailUrl) {
        setThumbnailUrl(thumbnailUrl);
    }


    private boolean canAvailable() {
        return this.status == LectureStatus.UNAVAILABLE;
    }
    // Publish rule:
    // - only UNAVAILABLE lecture can be made AVAILABLE
    // - must have >= 1 chapter
    // - must have >= 1 lesson across all chapters
    private void validateForAvailable() {
        // TODO(Policy): 추후 LecturePublishPolicy로 추출할지 결정하고 이동
        if (!canAvailable()) {
            throw new LectureAlreadyPublishedException();
        }
        if (chapters.isEmpty()) {
            throw new LectureCannotPublishedWithoutChapterException();
        }
        boolean hasLesson = chapters.stream()
                .anyMatch(chapter -> !chapter.getLessons().isEmpty());
        if (!hasLesson) {
            throw new LectureCannotPublishedWithoutLessonException();
        }
    }

    // Soft delete lecture:
    // - if already deleted => throw
    // - set deleteFlag=true and deletedAt=now
    public void softDelete() {
        // TODO(Invariant): deleteFlag=true <-> deletedAt!=null (가능하면 불변조건으로 강제)
        validateCanDelete();

        this.deleteFlag = true;
        this.deletedAt = Instant.now();
    }


    private void validateCanDelete() {
        if (this.isDeleteFlag()) {
            throw new LectureDeletedException();
        }
    }

    // Restore:
    // - only when deleteFlag=true
    // - set deleteFlag=false and deletedAt=null
    void restore() {
        // TODO(Access): restore의 접근 범위(package-private)가 의도인지 확인
        if (!this.isDeleteFlag()) {
            throw new LectureNotDeletedException();
        }
        this.deleteFlag = false;
        this.deletedAt = null;
    }


    public void makeSubmitted() {
        validateForAvailable();
        this.status = LectureStatus.SUBMITTED;
    }

    // Only Admin can call this method
    public void notAllowPublish() {
        this.status = LectureStatus.REJECTED;
    }
    
    // Only Admin can call this method
    public void allowPublish() {
        this.status = LectureStatus.AVAILABLE;
    }

    public Set<Chapter> getChapters() {
        return Collections.unmodifiableSet(chapters);
    }

    public int getTotalLessonCount() {
        return chapters.stream()
                .mapToInt(chapter -> chapter.getLessons().size())
                .sum();
    }
    

    // Get total count of chapters in this lecture
    public int getTotalChapterCount() {
        return chapters.size();

    }

    // lecture/domain/policy/LecturePublishPolicy.java
    // public interface LecturePublishPolicy {
    // boolean canPublish(Lecture lecture);
    
    // 구현체에서 "최소 1개 챕터, 1개 레슨" 등의 규칙 검증
// }

}
