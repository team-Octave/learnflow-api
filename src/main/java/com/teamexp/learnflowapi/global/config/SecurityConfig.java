package com.teamexp.learnflowapi.global.config;

import com.teamexp.learnflowapi.global.common.filter.LogTraceFilter;
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

/**
 * 애플리케이션 보안 설정을 담당하는 클래스입니다.
 * 필터 체인 구성 및 권한 관리를 수행합니다.
 */
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

    /**
     * TraceID 발급 필터를 빈으로 등록합니다.
     * 코드래빗 피드백: @Component와 addFilterBefore 중복 등록을 방지하기 위해 수동 등록합니다.
     */
    @Bean
    public LogTraceFilter logTraceFilter() {
        return new LogTraceFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // 1. 최상단: 인가 불필요 경로
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/check").permitAll()

                // 2. 구체적인 인증 필요 경로 (MEMBER 전용 조회)
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/my").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").hasAnyRole("MEMBER", "ADMIN")

                // 3. ADMIN 전용 경로
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // 4. MEMBER 전용 (쓰기/수정/삭제 작업)
                .requestMatchers(HttpMethod.POST, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/lectures/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").hasRole("MEMBER")
                .requestMatchers("/api/v1/enrollment/**", "/api/v1/contents/**").hasRole("MEMBER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").hasRole("MEMBER")

                // 5. 일반 조회 API (누구나 가능)
                .requestMatchers(HttpMethod.GET, "/api/v1/lectures/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/reviews/lectures/**").permitAll()

                // 6. 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService);

        // 필터 순서: 로깅(TraceID) -> JWT 인증 -> 표준 인증 필터 순으로 실행
        http
            .addFilterBefore(logTraceFilter(), UsernamePasswordAuthenticationFilter.class)
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
