package com.teamexp.learnflowapi.auth.service;

import com.teamexp.learnflowapi.auth.controller.dto.LoginRequest;
import com.teamexp.learnflowapi.auth.controller.dto.LoginResponse;
import com.teamexp.learnflowapi.auth.controller.dto.ReissuanceResponse;
import com.teamexp.learnflowapi.auth.exception.RefreshTokenInvalidException;
import com.teamexp.learnflowapi.auth.exception.UserNotFoundException;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                       UserRepository userRepository, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
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
            user.getId(), user.getEmail(), user.getRole().name(), user.getNickname()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // 4. DTO로 맵핑
        return new LoginResponse(accessToken, refreshToken);
    }

    public ReissuanceResponse reissueToken(String refreshToken) {
        // 검증 로직 추가(토큰이 비어있는 경우)
        if (refreshToken == null) {
            throw new RefreshTokenInvalidException();
        }

        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RefreshTokenInvalidException();
        }

        // 토큰에서 사용자 정보 추출
        Claims claims = jwtTokenProvider.parseToken(refreshToken);
        String userId = claims.getSubject();

        // refresh Token을 DB에서 검증하는 로직
        if (!tokenService.validateToken(refreshToken, userId)) {
            throw new RefreshTokenInvalidException();
        }

        // 유저 정보 조회
        User findUser = userRepository.findById(userId).orElseThrow(
            UserNotFoundException::new
        );


        // 새로운 액세스 토큰 & 리프레시 토큰 발급 (RTR 방식)
        String newAccessToken = jwtTokenProvider.createAccessToken(userId, findUser.getEmail(), findUser.getRole().name(), findUser.getNickname());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        // 새로 발급한 refresh token DB에 저장하는 로직 필요 (RTR 방식)
        tokenService.storeToken(newRefreshToken, userId);

        // DTO로 반환하거나 필요한 작업 수행
        return new ReissuanceResponse(newAccessToken, newRefreshToken);
    }
}
