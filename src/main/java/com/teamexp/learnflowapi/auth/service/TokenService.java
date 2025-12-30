package com.teamexp.learnflowapi.auth.service;

import com.teamexp.learnflowapi.auth.exception.RefreshTokenInvalidException;
import com.teamexp.learnflowapi.auth.model.Token;
import com.teamexp.learnflowapi.auth.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void issueRefreshToken(String userId, String refreshToken) {
        tokenRepository.findByUserId(userId)
            .ifPresentOrElse(
                existing -> existing.rotate(refreshToken),
                () -> tokenRepository.save(Token.createToken(userId, refreshToken))
            );
    }
    @Transactional
    public void rotateRefreshToken(String userId, String oldToken, String newToken) {

        Token token = tokenRepository.findByUserId(userId)
            .orElseThrow(RefreshTokenInvalidException::new);

        // ❗ 여기서 RTR의 핵심
        if (!token.getToken().equals(oldToken)) {
            throw new RefreshTokenInvalidException();
        }

        // 기존 refresh token은 여기서 "소모됨"
        token.rotate(newToken);
    }

    @Transactional
    public void revokeRefreshToken(String userId, String refreshToken) {

        Token token = tokenRepository.findByUserId(userId)
            .orElseThrow(RefreshTokenInvalidException::new);

        // 현재 세션의 토큰이 맞는지 확인
        if (!token.getToken().equals(refreshToken)) {
            throw new RefreshTokenInvalidException();
        }

        // 로그아웃 = refresh token 제거
        tokenRepository.delete(token);
    }

}
