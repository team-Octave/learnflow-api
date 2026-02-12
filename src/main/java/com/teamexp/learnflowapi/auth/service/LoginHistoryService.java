package com.teamexp.learnflowapi.auth.service;

import com.teamexp.learnflowapi.auth.model.LoginHistory;
import com.teamexp.learnflowapi.auth.repository.LoginHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 이력 저장 서비스
 * 비동기로 동작하여 로그인 응답 시간에 영향을 주지 않음
 */
@Slf4j
@Service
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    @Async("loginHistoryExecutor")
    @Transactional
    public void saveLoginHistory(String userId, String ipAddress, String userAgent) {
        try {
            LoginHistory history = LoginHistory.create(userId, ipAddress, userAgent);
            loginHistoryRepository.save(history);
            log.debug("로그인 이력 저장 완료: userId={}", userId);
        } catch (Exception e) {
            // 로그인 이력 저장 실패해도 로그인 플로우에 영향 주지 않음
            log.error("로그인 이력 저장 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
}
