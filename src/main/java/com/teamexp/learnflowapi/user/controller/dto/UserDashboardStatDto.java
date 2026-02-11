package com.teamexp.learnflowapi.user.controller.dto;

public record UserDashboardStatDto(
        long totalActiveUsers,      // 활동 회원
        long totalChurnedUsers,     // 탈퇴 회원
        long newUsersToday,         // 오늘 가입
        long churnedUsersToday,     // 오늘 탈퇴
        long subscribedUsers,       // 구독자
        long nonSubscribedUsers     // 비구독자
) {}
