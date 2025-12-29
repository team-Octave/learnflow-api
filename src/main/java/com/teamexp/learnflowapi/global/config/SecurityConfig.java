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
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()          // 회원가입
                        .requestMatchers("/api/v1/users/check").permitAll()    // 닉네임 체크
                        .requestMatchers("/api/v1/auth/login").permitAll()    // 로그인
                        .requestMatchers("/api/v1/auth/reissue").permitAll()   // 토큰 재발급
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures").permitAll() // 강의 전체 조회
                        .requestMatchers(HttpMethod.GET, "/api/v1/lectures/*").permitAll() // 강의 단건 조회
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/lectures/*").permitAll() // 강의별 리뷰 조회
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
