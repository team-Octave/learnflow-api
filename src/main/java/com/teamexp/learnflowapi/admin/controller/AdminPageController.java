package com.teamexp.learnflowapi.admin.controller;

import com.teamexp.learnflowapi.admin.dto.AdminDashboardDto;
import com.teamexp.learnflowapi.admin.dto.SettlementDto;
import com.teamexp.learnflowapi.admin.service.AdminDashboardService;
import com.teamexp.learnflowapi.admin.service.SettlementService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * 관리자 페이지 컨트롤러 (Thymeleaf 뷰 반환)
 */
@Controller
public class AdminPageController {

    private final AdminDashboardService adminDashboardService;
    private final SettlementService settlementService;

    public AdminPageController(AdminDashboardService adminDashboardService, SettlementService settlementService) {
        this.adminDashboardService = adminDashboardService;
        this.settlementService = settlementService;
    }

    // ... (중략) ...

    /**
     * 정산 페이지
     * 실제 정산 데이터 연결
     */
    @GetMapping("/admin/settlement")
    public String settlement(Model model) {
        List<SettlementDto> rows = settlementService.getSettlementList();
        long totalSettlementAmount = rows.stream().mapToLong(SettlementDto::settlementAmount).sum();

        model.addAttribute("rows", rows);
        model.addAttribute("totalCompletedCount", totalSettlementAmount);
        
        return "admin/settlement";
    }

    /**
     * 정산 내역 엑셀 다운로드
     */
    @GetMapping("/admin/settlement/download")
    public ResponseEntity<InputStreamResource> downloadSettlementExcel() {
        ByteArrayInputStream in = settlementService.createExcel();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=settlement.xlsx");

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new InputStreamResource(in));
    }
}
