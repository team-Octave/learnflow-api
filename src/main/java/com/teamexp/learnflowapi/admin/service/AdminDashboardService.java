package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto;
import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto.DailyStatDto;
import com.teamexp.learnflowapi.auth.repository.LoginHistoryRepository;
import com.teamexp.learnflowapi.log.repository.TrackingRepository;

import com.teamexp.learnflowapi.user.controller.dto.UserDashboardStatDto;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import com.teamexp.learnflowapi.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final TrackingRepository trackingRepository;
    private final UserService userService;

    public AdminDashboardService(UserRepository userRepository,
                                 LoginHistoryRepository loginHistoryRepository,
                                 TrackingRepository trackingRepository,
                                 UserService userService) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.trackingRepository = trackingRepository;
        this.userService = userService;
    }

    public AdminDashboardDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.systemDefault();

        Instant todayStart = today.atStartOfDay(zoneId).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        LocalDate weekAgoDate = today.minusDays(6);
        Instant weekStart = weekAgoDate.atStartOfDay(zoneId).toInstant();

        //카드형 통계
        long totalUsers = userRepository.countByDelFlagFalse();
        long newUsersToday = userRepository.countByCreatedAtBetweenAndDelFlagFalse(todayStart, todayEnd);
        long churnedUsers = userRepository.countByDelFlagTrue();

        Long dauCount = loginHistoryRepository.countDistinctUserByLoginAtBetween(todayStart, todayEnd);
        long dauToday = (dauCount != null) ? dauCount : 0L;

        //가입자
        List<Object[]> signupStats = userRepository.findDailySignupStats(weekStart, todayEnd);
        List<DailyStatDto> weeklyNewUsers = fillMissingDates(signupStats, weekAgoDate, 7);

        //DAU
        List<Object[]> dauStats = loginHistoryRepository.findDailyActiveUsers(weekStart, todayEnd);
        List<DailyStatDto> weeklyDau = fillMissingDates(dauStats, weekAgoDate, 7);

        //Referrer
        List<Object[]> referrerStats = trackingRepository.findReferrerStats();
        Map<String, Long> referrerDistribution = convertStatsToMap(referrerStats);

        //이탈 페이지(Exit Page)
        List<Object[]> exitStats = trackingRepository.findExitPageStats();
        Map<String, Long> exitPageDistribution = convertStatsToMap(exitStats);

        UserDashboardStatDto userStats = userService.getUserStatistics();

        return new AdminDashboardDto(
                totalUsers,
                newUsersToday,
                churnedUsers,
                dauToday,
                referrerDistribution,
                exitPageDistribution,
                weeklyNewUsers,
                weeklyDau,
                userStats
        );
    }

    private Map<String, Long> convertStatsToMap(List<Object[]> stats) {
        Map<String, Long> result = stats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],                 // Key (URL 또는 Path)
                        row -> ((Number) row[1]).longValue(),   // Value (Count)
                        (oldVal, newVal) -> oldVal,             // 키 중복 시 기존 값 유지
                        LinkedHashMap::new                      // 순서 보장 (쿼리 정렬 유지)
                ));

        // 데이터가 없으면 "데이터 수집 중" 표시
        if (result.isEmpty()) {
            result.put("데이터 수집 중", 0L);
        }

        return result;
    }

    // (기존 메서드 유지) 날짜 채우기
    private List<DailyStatDto> fillMissingDates(List<Object[]> rawData, LocalDate startDate, int days) {
        Map<String, Long> statMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> ((Number) row[1]).longValue(),
                        (v1, v2) -> v1
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