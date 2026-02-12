package com.teamexp.learnflowapi.admin.repository.projection;

/**
 * Key-Count 쌍 통계 Projection (유입경로, 이탈페이지)
 * JPQL alias: key, count
 */
public interface KeyCountProjection {
    String getKey();
    Long getCount();
}
