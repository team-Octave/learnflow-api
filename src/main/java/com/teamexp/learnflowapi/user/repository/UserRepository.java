package com.teamexp.learnflowapi.user.repository;

import com.teamexp.learnflowapi.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
