package com.teamexp.learnflowapi.admin.dto;

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
    List<DailyStatDto> weeklyDau
) {
    public record DailyStatDto(String date, long count) {}
}
