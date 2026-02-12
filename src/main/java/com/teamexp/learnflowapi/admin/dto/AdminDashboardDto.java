package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.admin.service.dto.UserRateMembershipDto;

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
    UserRateMembershipDto userRateMembershipDto
) {
    public record DailyStatDto(String date, long count) {}
}
