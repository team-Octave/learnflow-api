package com.teamexp.learnflowapi.user.service;

import com.teamexp.learnflowapi.global.config.PasswordConfig;
import com.teamexp.learnflowapi.user.controller.dto.UserCreateRequest;
import com.teamexp.learnflowapi.user.exception.EmailDuplicatedException;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordConfig passwordConfig;

    @Autowired
    public UserService(UserRepository userRepository, PasswordConfig passwordConfig) {
        this.userRepository = userRepository;
        this.passwordConfig = passwordConfig;
    }

    @Transactional
    public void register(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailDuplicatedException();
        }

        // password 암호화
        String encodedPassword = passwordConfig.passwordEncoder().encode(request.password());


        // TODO : 현재 user의 Role은 Member밖에 없는 상황
        User user = User.createUser(request.email(), encodedPassword,request.nickname(), UserRole.MEMBER);

        userRepository.save(user);

    }
}
