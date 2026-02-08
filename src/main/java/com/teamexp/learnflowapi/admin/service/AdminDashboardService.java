package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto;
import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto.DailyStatDto;
import com.teamexp.learnflowapi.auth.repository.LoginHistoryRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;

    public AdminDashboardService(UserRepository userRepository, LoginHistoryRepository loginHistoryRepository) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public AdminDashboardDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();

        Instant todayStart = today.atStartOfDay(zoneId).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();
        
        // 최근 7일 (오늘 포함)
        LocalDate weekAgoDate = today.minusDays(6);
        Instant weekStart = weekAgoDate.atStartOfDay(zoneId).toInstant();

        // 1. 카드형 통계
        long totalUsers = userRepository.countByDelFlagFalse();
        long newUsersToday = userRepository.countByCreatedAtBetweenAndDelFlagFalse(todayStart, todayEnd);
        long churnedUsers = userRepository.countByDelFlagTrue();
        
        // LoginHistory 테이블이 비어있을 경우 null 반환 방지
        Long dauCount = loginHistoryRepository.countDistinctUserByLoginAtBetween(todayStart, todayEnd);
        long dauToday = (dauCount != null) ? dauCount : 0L;

        // 2. 차트 데이터 (가입자)
        List<Object[]> signupStats = userRepository.findDailySignupStats(weekStart, todayEnd);
        List<DailyStatDto> weeklyNewUsers = fillMissingDates(signupStats, weekAgoDate, 7);

        // 3. 차트 데이터 (DAU)
        List<Object[]> dauStats = loginHistoryRepository.findDailyActiveUsers(weekStart, todayEnd);
        List<DailyStatDto> weeklyDau = fillMissingDates(dauStats, weekAgoDate, 7);

        // 4. 유입 경로 (더미 - 나중에 User 엔티티에 referrer 추가 시 구현)
        Map<String, Long> referrerDistribution = Map.of(
            "직접 유입", 100L,
            "검색", 50L,
            "SNS", 20L
        );

        return new AdminDashboardDto(
            totalUsers,
            newUsersToday,
            churnedUsers,
            dauToday,
            referrerDistribution,
            weeklyNewUsers,
            weeklyDau
        );
    }

    /**
     * DB에서 조회한 날짜별 통계 데이터를 바탕으로, 누락된 날짜를 0으로 채워서 반환
     */
    private List<DailyStatDto> fillMissingDates(List<Object[]> rawData, LocalDate startDate, int days) {
        // DB 결과를 Map으로 변환 (Key: 날짜문자열, Value: count)
        Map<String, Long> statMap = rawData.stream()
            .collect(Collectors.toMap(
                row -> row[0].toString(), // DATE 함수 결과 (YYYY-MM-DD)
                row -> ((Number) row[1]).longValue(),
                (v1, v2) -> v1 // 중복 발생 시 첫 번째 값
            ));

        List<DailyStatDto> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(formatter);
            long count = statMap.getOrDefault(dateStr, 0L);
            result.add(new DailyStatDto(dateStr, count));
        }

        return result;
    }
}
