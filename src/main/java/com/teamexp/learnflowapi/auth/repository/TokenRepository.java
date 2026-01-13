package com.teamexp.learnflowapi.auth.repository;

import com.teamexp.learnflowapi.auth.model.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    boolean existsByTokenAndUserId(String token, String userId);

    Optional<Token> findByUserId(String userId);
}
