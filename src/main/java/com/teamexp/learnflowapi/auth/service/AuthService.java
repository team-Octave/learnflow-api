package com.teamexp.learnflowapi.auth.service;

import com.teamexp.learnflowapi.auth.controller.dto.LoginRequest;
import com.teamexp.learnflowapi.auth.controller.dto.LoginResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 스프링 시큐리티 인증 시도 (이메일/비번)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // 2. 인증 성공 → UserDetails 꺼내기
        CustomUserPrincipal user = (CustomUserPrincipal) authentication.getPrincipal();

        // 3. 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(), user.getEmail(), user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());


        // 4. DTO로 맵핑
        return new LoginResponse(accessToken, refreshToken);
    }
}
