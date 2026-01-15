package com.teamexp.learnflowapi.global.config;

import com.teamexp.learnflowapi.global.security.exception.JwtAuthenticationEntryPoint;
import com.teamexp.learnflowapi.global.security.jwt.JwtAuthenticationFilter;
import com.teamexp.learnflowapi.user.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordConfig passwordConfig;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService, PasswordConfig passwordConfig,
                          JwtAuthenticationFilter jwtAuthenticationFilter, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.passwordConfig = passwordConfig;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // CORS 설정 적용.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // 1. ADMIN 전용 (가장 엄격)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 2. MEMBER 전용 (수정/삭제/생성 등 쓰기 작업 우선 배치) - TODO : 리팩토링 되면 수정해야 함.
                        .requestMatchers(HttpMethod.POST, "/api/v1/lectures/**").hasRole("MEMBER")              // 생성
                        .requestMatchers(HttpMethod.PUT, "/api/v1/lectures/**").hasRole("MEMBER")               // 업데이트
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/lectures/**").hasRole("MEMBER")            // 삭제
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/my").hasRole("MEMBER")               // 내 강의 조회
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").hasRole("MEMBER")                // 리뷰 업데이트
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").hasRole("MEMBER")             // 리뷰 삭제
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").hasRole("MEMBER")               // 리뷰 생성
                        .requestMatchers("/api/v1/enrollment/**", "/api/v1/contents/**").hasRole("MEMBER")    // content, enrollment 모든 API
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").hasRole("MEMBER")               // 회원 탈퇴

                        // 3. Permit All (조회 및 공용 API)
                        .requestMatchers("/actuator/health", "/actuator/prometheus", "/actuator/info").permitAll()
                        .requestMatchers( "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/check").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()             // 모든 GET 조회 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()              // 모든 GET 조회 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/lectures/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/me").permitAll()                // 내 정보 조회

                        // 4. 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService);

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordConfig.passwordEncoder());

        return builder.build();
    }
}
