package com.teamexp.learnflowapi.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/**
 * 관리자 페이지 컨트롤러 (Thymeleaf 뷰 반환)
 * TASK 0: 껍데기 확인용 더미 데이터 제공
 */
@Controller
public class AdminPageController {

    /**
     * 관리자 로그인 페이지
     */
    @GetMapping("/admin-login")
    public String loginPage() {
        return "admin/login";
    }

    /**
     * 관리자 대시보드 페이지
     * 더미 데이터로 HTML 껍데기 확인
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        // 더미 데이터 - TASK 2에서 실제 서비스로 교체 예정
        model.addAttribute("totalUsers", 1234);
        model.addAttribute("newUsersToday", 12);
        model.addAttribute("churnedUsersTotal", 45);
        model.addAttribute("dauToday", 148);
        
        model.addAttribute("referrerDistribution", Map.of(
            "직접 유입", 800L,
            "검색", 300L,
            "SNS", 134L
        ));
        
        model.addAttribute("weeklyNewUsers", List.of(
            new DailyStatDto("2026-02-01", 10),
            new DailyStatDto("2026-02-02", 15),
            new DailyStatDto("2026-02-03", 8),
            new DailyStatDto("2026-02-04", 20),
            new DailyStatDto("2026-02-05", 12),
            new DailyStatDto("2026-02-06", 18),
            new DailyStatDto("2026-02-07", 12)
        ));
        
        model.addAttribute("weeklyDau", List.of(
            new DailyStatDto("2026-02-01", 120),
            new DailyStatDto("2026-02-02", 135),
            new DailyStatDto("2026-02-03", 110),
            new DailyStatDto("2026-02-04", 150),
            new DailyStatDto("2026-02-05", 140),
            new DailyStatDto("2026-02-06", 155),
            new DailyStatDto("2026-02-07", 148)
        ));
        
        model.addAttribute("recentUsers", List.of());  // 빈 리스트 (TASK 3에서 구현)
        
        return "admin/index";
    }

    /**
     * 정산 페이지
     * 더미 데이터로 HTML 껍데기 확인
     */
    @GetMapping("/admin/settlement")
    public String settlement(Model model) {
        // 더미 데이터 - TASK 4에서 실제 서비스로 교체 예정
        model.addAttribute("rows", List.of());
        model.addAttribute("totalCompletedCount", 0L);
        return "admin/settlement";
    }

    /**
     * 일별 통계 DTO (더미 데이터용)
     * TASK 2에서 admin/dto/ 패키지로 이동 예정
     */
    record DailyStatDto(String date, long count) {}
}
