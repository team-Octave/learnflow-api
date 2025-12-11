package com.teamexp.learnflowapi.user.service;

import com.teamexp.learnflowapi.auth.exception.UserNotFoundException;
import com.teamexp.learnflowapi.global.config.PasswordConfig;
import com.teamexp.learnflowapi.user.controller.dto.NicknameCheckResponse;
import com.teamexp.learnflowapi.user.controller.dto.UserCreateRequest;
import com.teamexp.learnflowapi.user.controller.dto.UserReadResponse;
import com.teamexp.learnflowapi.user.exception.EmailDuplicatedException;
import com.teamexp.learnflowapi.user.exception.NicknameDuplicateException;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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

        if (userRepository.existsByNickname(request.nickname())) {
            throw new NicknameDuplicateException();
        }

        // password 암호화
        String encodedPassword = passwordConfig.passwordEncoder().encode(request.password());


        // TODO : 현재 user의 Role은 Member밖에 없는 상황
        User user = User.createUser(request.email(), encodedPassword,request.nickname(), UserRole.MEMBER);

        userRepository.save(user);

    }

    public NicknameCheckResponse checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new NicknameDuplicateException();
        }
        return new NicknameCheckResponse(true);
    }

    @Transactional
    public void withdrawUser(String userId) {
        userRepository.deleteById(userId);
    }

    public UserReadResponse getUserInfo(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
            UserNotFoundException::new
        );
        return new UserReadResponse(user.getNickname(), user.getEmail(), user.getRole().name());
    }
}
