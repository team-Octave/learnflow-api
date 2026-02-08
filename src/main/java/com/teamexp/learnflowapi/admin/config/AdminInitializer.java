package com.teamexp.learnflowapi.admin.config;

import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 개발 환경 전용 ADMIN 계정 초기화
 * production 환경에서는 이 빈이 로드되지 않음
 */
@Configuration
@Profile("local")
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@learnflow.com";

            // 기존 계정이 있으면 삭제 (비밀번호 초기화 및 상태 리셋을 위해)
            userRepository.findByEmail(adminEmail).ifPresent(user -> {
                userRepository.delete(user);
                System.out.println("🔄 기존 ADMIN 계정 삭제 완료 (초기화)");
            });

            // 계정 신규 생성
            User admin = User.createUser(
                    adminEmail,
                    passwordEncoder.encode("password"),
                    "관리자",
                    UserRole.ADMIN
            );
            userRepository.save(admin);
            System.out.println("✅ ADMIN 계정 생성/초기화 완료: " + adminEmail + " / password");
        };
    }

}
