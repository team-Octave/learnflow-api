package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.ContentMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ContentMediaRepository extends JpaRepository<ContentMedia,Long> {

    // 레슨 한개당 영상 1개
    Optional<ContentMedia> findByLessonId(Long lessonId);
    // 렉처 삭제시 같이 삭제
    void deleteByLessonId(Long lessonId);
}
