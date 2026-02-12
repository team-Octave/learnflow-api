package com.teamexp.learnflowapi.admin.dto;

public record SettlementDto(
    String instructorId,
    String instructorName,
    long totalSalesCount,   // 총 판매량
    long totalSalesAmount,  // 총 매출액
    long feeAmount,         // 수수료 (30%)
    long settlementAmount   // 정산금
) {}
