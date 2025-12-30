package com.teamexp.learnflowapi.auth.service;

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
    public boolean validateToken(String token, String userId) {

        // DB에서 토큰과 사용자 ID로 토큰 조회
        return tokenRepository.existsByTokenAndUserId(token, userId);
    }

    @Transactional
    public void storeToken(String token, String userId) {
        tokenRepository.findByUserId(userId)
            .ifPresentOrElse(
                existing -> existing.rotate(token),
                () -> tokenRepository.save(Token.createToken(userId, token))
            );
    }

}
