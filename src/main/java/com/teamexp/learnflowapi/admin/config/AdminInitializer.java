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
@Profile({"local", "dev","test"}) // default 프로필에서도 실행되도록 추가
public class AdminInitializer {

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@learnflow.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.createUser(
                        adminEmail,
                        passwordEncoder.encode("adminPassword"),
                        "Admin",
                        UserRole.ADMIN
                );
                userRepository.save(admin);
                System.out.println("✅ ADMIN 계정 생성/초기화 완료 (Email : " + adminEmail + ")");
            } else {
                System.out.println("ℹ️ ADMIN 계정이 이미 존재합니다: " + adminEmail);
            }
        };
    }
}

