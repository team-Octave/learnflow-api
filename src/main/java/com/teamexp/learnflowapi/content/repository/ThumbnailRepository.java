package com.teamexp.learnflowapi.content.repository;

import com.teamexp.learnflowapi.content.model.Thumbnail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface ThumbnailRepository extends JpaRepository<Thumbnail,Long> {

    // 렉처 하나당 썸네일
    Optional<Thumbnail> findByLectureId(Long lectureId);

    @Deprecated // TODO: 모든 lecture의 thumbnailUrl 역정규화 완료 후 제거, 사실 그냥 thumbnai 자체가 필요없음
    String findFileUrlById(Long thumbnailId);

    // 렉처 삭제시 같이 삭제
    void deleteByLectureId(Long lectureId);
}
