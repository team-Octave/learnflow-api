package com.teamexp.learnflowapi.global.config;

import com.teamexp.learnflowapi.global.common.filter.LogTraceFilter;
import com.teamexp.learnflowapi.global.common.filter.RequestResponseLoggingFilter; // 1. 필터 임포트 추가
import com.teamexp.learnflowapi.global.security.exception.JwtAuthenticationEntryPoint;
import com.teamexp.learnflowapi.global.security.jwt.JwtAuthenticationFilter;
import com.teamexp.learnflowapi.user.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordConfig passwordConfig,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.passwordConfig = passwordConfig;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public LogTraceFilter logTraceFilter() {
        return new LogTraceFilter();
    }

    /**
     * 바디 로깅 필터를 빈으로 등록합니다.
     * LogTraceFilter와 동일한 이유로 중복 등록 방지를 위해 수동 등록합니다.
     */
    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter() {
        return new RequestResponseLoggingFilter();
    }

    /**
     * Admin 페이지용 SecurityFilterChain (Form Login + Session 기반)
     * Order(1)로 /admin/** 경로를 먼저 매칭
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/admin/**", "/admin-login")
            .csrf(AbstractHttpConfigurer::disable)  // Phase 1에서는 CSRF 비활성화
            .formLogin(form -> form
                .loginPage("/admin-login")
                .loginProcessingUrl("/admin-login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin-login")
                .permitAll())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin-login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .userDetailsService(userDetailsService)
            .build();
    }

    /**
     * REST API용 SecurityFilterChain (JWT 기반)
     * Order(2)로 /api/** 경로를 나중에 매칭
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**", "/actuator/**")
            .cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/check").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/my").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers("/api/v1/enrollment/**", "/api/v1/contents/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/lectures/**").permitAll()
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService);

        // 🎯 필터 실행 순서 정의
        http
            // 1. 가장 먼저 TraceID 생성 (MDC 주입)
            .addFilterBefore(logTraceFilter(), UsernamePasswordAuthenticationFilter.class)
            // 2. 생성된 ID를 가지고 요청/응답 Body 로깅 실행
            .addFilterAfter(requestResponseLoggingFilter(), LogTraceFilter.class)
            // 3. 이후 JWT 인증 진행
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
