package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.ReferencedVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferencedVideoRepository extends JpaRepository<ReferencedVideo, Long> {

    Optional<ReferencedVideo> findByLessonId(Long lessonId);
}
