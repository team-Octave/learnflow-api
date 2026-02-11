package com.teamexp.learnflowapi.user.repository;

import com.teamexp.learnflowapi.user.model.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserIdAndDelFlagFalse(String userId);

    /* --- Admin 통계용 쿼리 --- */

    // 전체 활성 회원 수
    long countByDelFlagFalse();

    // 특정 기간 가입자 수 (오늘 등)
    long countByCreatedAtBetweenAndDelFlagFalse(Instant start, Instant end);

    // 총 탈퇴 회원 수
    long countByDelFlagTrue();

    // 일별 가입자 추이 (차트용)
    @Query("SELECT FUNCTION('DATE', u.createdAt) as date, COUNT(u) as count " +
           "FROM User u " +
           "WHERE u.createdAt BETWEEN :start AND :end AND u.delFlag = false " +
           "GROUP BY FUNCTION('DATE', u.createdAt) " +
           "ORDER BY date ASC")
    List<Object[]> findDailySignupStats(@Param("start") Instant start, @Param("end") Instant end);

    // BackLogService용 - delFlag 기준 카운트
    Long countAllByDelFlagIs(boolean delFlag);
}
