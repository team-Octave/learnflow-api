package com.teamexp.learnflowapi.admin.repository;

import com.teamexp.learnflowapi.admin.model.Approval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRepository extends JpaRepository<Approval, Long> {

    /**
     * 특정 강의의 가장 최근 Approval 조회 (반려 사유 확인용)
     */
    Optional<Approval> findTopByLectureIdOrderByUpdatedAtDesc(Long lectureId);

    /**
     * 여러 강의의 가장 최근 Approval 배치 조회 (N+1 방지)
     * 각 강의별로 가장 최근의 Approval만 반환
     */
    @Query("""
        SELECT a FROM Approval a
        WHERE a.lectureId IN :lectureIds
        AND a.updatedAt = (
            SELECT MAX(a2.updatedAt) FROM Approval a2 WHERE a2.lectureId = a.lectureId
        )
        """)
    List<Approval> findLatestByLectureIds(List<Long> lectureIds);
}
