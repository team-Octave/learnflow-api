package com.teamexp.learnflowapi.admin.dto;

import com.teamexp.learnflowapi.admin.service.dto.UserRateMembershipDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardDto {
    private final long totalUsers;
    private final long newUsersToday;
    private final long churnedUsersTotal;
    private final long dauToday;
    private final Map<String, Long> referrerDistribution;
    private final Map<String, Long> exitPageDistribution;
    private final List<DailyStatDto> weeklyNewUsers;
    private final List<DailyStatDto> weeklyDau;
    private final UserRateMembershipDto userRateMembershipDto;

    public record DailyStatDto(String date, long count) {}
}
