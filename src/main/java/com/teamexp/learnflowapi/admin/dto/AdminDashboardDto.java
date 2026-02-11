package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.user.controller.dto.UserDashboardStatDto;

import java.util.List;
import java.util.Map;

public record AdminDashboardDto(
    long totalUsers,
    long newUsersToday,
    long churnedUsersTotal,
    long dauToday,
    Map<String, Long> referrerDistribution,
    Map<String, Long> exitPageDistribution,
    List<DailyStatDto> weeklyNewUsers,
    List<DailyStatDto> weeklyDau,
    UserDashboardStatDto userStats
) {
    public record DailyStatDto(String date, long count) {}
}
