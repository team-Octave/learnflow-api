package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Thumbnail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThumbnailRepository extends JpaRepository<Thumbnail,Long> {

    // 렉처 하나당 썸네일
    Optional<Thumbnail> findByLectureId(Long lectureId);

    // 렉처 삭제시 같이 삭제
    void deleteByLectureId(Long lectureId);
}
