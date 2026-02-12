package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto;
import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto.DailyStatDto;
import com.teamexp.learnflowapi.admin.repository.projection.DailyStatProjection;
import com.teamexp.learnflowapi.admin.repository.projection.KeyCountProjection;
import com.teamexp.learnflowapi.admin.service.dto.UserRateMembershipDto;
import com.teamexp.learnflowapi.auth.repository.LoginHistoryRepository;
import com.teamexp.learnflowapi.log.repository.TrackingRepository;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
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
    private final MembershipRepository membershipRepository;

    public AdminDashboardService(UserRepository userRepository,
                                 LoginHistoryRepository loginHistoryRepository,
                                 TrackingRepository trackingRepository,
                                 MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.trackingRepository = trackingRepository;
        this.membershipRepository = membershipRepository;
    }

    public AdminDashboardDto getDashboardStats() {
        LocalDate today = LocalDate.now();
        ZoneId zoneId = ZoneId.of("Asia/Seoul");

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
        List<DailyStatProjection> signupStats = userRepository.findDailySignupStats(weekStart, todayEnd);
        List<DailyStatDto> weeklyNewUsers = fillMissingDates(signupStats, weekAgoDate, 7);

        //DAU
        List<DailyStatProjection> dauStats = loginHistoryRepository.findDailyActiveUsers(weekStart, todayEnd);
        List<DailyStatDto> weeklyDau = fillMissingDates(dauStats, weekAgoDate, 7);

        //Referrer
        List<KeyCountProjection> referrerStats = trackingRepository.findReferrerStats();
        Map<String, Long> referrerDistribution = convertStatsToMap(referrerStats);

        //이탈 페이지(Exit Page)
        List<KeyCountProjection> exitStats = trackingRepository.findExitPageStats();
        Map<String, Long> exitPageDistribution = convertStatsToMap(exitStats);

        // 구독 현황
        UserRateMembershipDto membershipDto = calculateMembershipStats();

        return AdminDashboardDto.builder()
                .totalUsers(totalUsers)
                .newUsersToday(newUsersToday)
                .churnedUsersTotal(churnedUsers)
                .dauToday(dauToday)
                .referrerDistribution(referrerDistribution)
                .exitPageDistribution(exitPageDistribution)
                .weeklyNewUsers(weeklyNewUsers)
                .weeklyDau(weeklyDau)
                .userRateMembershipDto(membershipDto)
                .build();
    }

    private UserRateMembershipDto calculateMembershipStats() {
        long totalUserCount = userRepository.countByDelFlagFalse();
        long membershipCount = membershipRepository.countAllByExpiredAtAfter(Instant.now());
        long normalCount = totalUserCount - membershipCount;

        double membershipRate = totalUserCount == 0 ? 0.0 : (double) membershipCount / totalUserCount * 100;
        double normalRate = totalUserCount == 0 ? 0.0 : (double) normalCount / totalUserCount * 100;

        return new UserRateMembershipDto(
                totalUserCount,
                membershipCount,
                normalCount,
                Math.round(normalRate * 10) / 10.0,
                Math.round(membershipRate * 10) / 10.0
        );
    }

    private Map<String, Long> convertStatsToMap(List<KeyCountProjection> stats) {
        Map<String, Long> result = stats.stream()
                .collect(Collectors.toMap(
                        KeyCountProjection::getKey,
                        KeyCountProjection::getCount,
                        (oldVal, newVal) -> oldVal,             // 키 중복 시 기존 값 유지
                        LinkedHashMap::new                      // 순서 보장 (쿼리 정렬 유지)
                ));

        // 데이터가 없으면 "데이터 수집 중" 표시
        if (result.isEmpty()) {
            result.put("데이터 수집 중", 0L);
        }

        return result;
    }


    private List<DailyStatDto> fillMissingDates(List<DailyStatProjection> rawData, LocalDate startDate, int days) {
        if (rawData == null) {
            rawData = new ArrayList<>();
        }
        Map<String, Long> statMap = rawData.stream()
                .collect(Collectors.toMap(
                        DailyStatProjection::getDate,
                        DailyStatProjection::getCount,
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
