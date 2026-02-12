package com.teamexp.learnflowapi.admin.repository.projection;

/**
 * 일별 통계 Projection (가입자 추이, DAU 추이)
 * JPQL alias: date, count
 */
public interface DailyStatProjection {
    String getDate();
    Long getCount();
}
