package com.teamexp.learnflowapi.global.config;

import com.teamexp.learnflowapi.global.common.filter.LogTraceFilter;
import com.teamexp.learnflowapi.global.common.filter.RequestResponseLoggingFilter;
import com.teamexp.learnflowapi.global.security.exception.JwtAuthenticationEntryPoint;
import com.teamexp.learnflowapi.global.security.jwt.JwtAuthenticationFilter;
import com.teamexp.learnflowapi.user.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired; // @Autowired 사용을 위해 추가
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

    @Bean
    public RequestResponseLoggingFilter requestResponseLoggingFilter() {
        return new RequestResponseLoggingFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))

            // 🔒 [권한 설정]
            .authorizeHttpRequests(auth -> auth
                // --- 기존 허용 URL ---
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/check").permitAll()

                // ✨ [NEW] AI 시스템 관련 허용 URL 추가
                .requestMatchers("/api/internal/**").permitAll()   // AI 서버가 호출하는 내부 API (Polling/Result)
                .requestMatchers("/api/ai/summary/**").permitAll() // 프론트엔드가 조회하는 요약 API (Public)

                // --- 역할별 권한 설정 ---
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/my").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // --- Member 권한 ---
                .requestMatchers(HttpMethod.POST, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers("/api/v1/enrollment/**", "/api/v1/contents/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").hasRole("MEMBER")

                // --- 조회 권한 (Public) ---
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/lectures/**").permitAll()

                .anyRequest().authenticated()
            );

        // 🎯 필터 실행 순서 정의
        http
            .addFilterBefore(logTraceFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(requestResponseLoggingFilter(), LogTraceFilter.class)
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
