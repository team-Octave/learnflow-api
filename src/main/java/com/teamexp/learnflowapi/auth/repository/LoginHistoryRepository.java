package com.teamexp.learnflowapi.auth.repository;

import com.teamexp.learnflowapi.admin.repository.projection.DailyStatProjection;
import com.teamexp.learnflowapi.auth.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 특정 기간 내 DAU (일별 활성 사용자 수) 조회
     * TASK 2 AdminDashboardService에서 사용
     */
    @Query("SELECT COUNT(DISTINCT lh.userId) FROM LoginHistory lh WHERE lh.loginAt BETWEEN :start AND :end")
    Long countDistinctUserByLoginAtBetween(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * 일별 DAU 추이 조회 (차트용)
     * TASK 2 AdminDashboardService에서 사용
     */
    @Query("SELECT FUNCTION('DATE', lh.loginAt) as date, COUNT(DISTINCT lh.userId) as count " +
           "FROM LoginHistory lh WHERE lh.loginAt BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('DATE', lh.loginAt) ORDER BY date")
    List<DailyStatProjection> findDailyActiveUsers(@Param("start") Instant start, @Param("end") Instant end);
}
