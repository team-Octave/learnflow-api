package com.teamexp.learnflowapi.user.service;

import com.teamexp.learnflowapi.global.config.PasswordConfig;
import com.teamexp.learnflowapi.membership.repository.MembershipRepository;
import com.teamexp.learnflowapi.user.controller.dto.UserCreateRequest;
import com.teamexp.learnflowapi.user.exception.EmailDuplicatedException;
import com.teamexp.learnflowapi.user.exception.NicknameDuplicateException;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordConfig passwordConfig;

    @Mock
    PasswordEncoder passwordEncoder;

    @Captor
    ArgumentCaptor<User> userCaptor;

    @Mock
    MembershipRepository membershipRepository;

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void tc1_register_fail_email_duplicate() {
        given(userRepository.existsByEmail("test@test.com"))
                .willReturn(true);

        UserCreateRequest request = new UserCreateRequest("test@test.com", "test1234", "nickname");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailDuplicatedException.class);

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void tc2_register_fail_nickname_duplicate() {
        given(userRepository.existsByEmail("test12@test.com"))
                .willReturn(false);
        given(userRepository.existsByNickname("nickname1"))
                .willReturn(true);

        UserCreateRequest request = new UserCreateRequest("test12@test.com", "test1234", "nickname1");

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(NicknameDuplicateException.class);

        verify(userRepository).existsByEmail("test12@test.com");
        verify(userRepository).existsByNickname("nickname1");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
        verify(passwordConfig, never()).passwordEncoder();
    }

    @Test
    @DisplayName("회원가입 성공 - 저장 수행")
    void tc3_register_success() {
        given(userRepository.existsByEmail("test@test.com"))
                .willReturn(false);
        given(userRepository.existsByNickname("nickname1"))
                .willReturn(false);
        given(passwordConfig.passwordEncoder())
                .willReturn(passwordEncoder);
        given(passwordEncoder.encode("test1234"))
                .willReturn("encodedPassword");

        UserCreateRequest request = new UserCreateRequest("test@test.com", "test1234", "nickname1");

        userService.register(request);

        verify(passwordEncoder).encode("test1234");
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.MEMBER);
    }

    @Test
    @DisplayName("닉네임 중복 체크 실패")
    void tc4_check_nickname_duplicate_fail() {
        given(userRepository.existsByNickname("nickname1"))
                .willReturn(true);

        assertThatThrownBy(() -> userService.checkNickname("nickname1"))
                .isInstanceOf(NicknameDuplicateException.class);

        verify(userRepository).existsByNickname("nickname1");
    }

    @Test
    @DisplayName("닉네임 중복 체크 성공")
    void tc5_check_nickname_duplicate_success() {
        given(userRepository.existsByNickname("uniqueNickname"))
                .willReturn(false);

        var response = userService.checkNickname("uniqueNickname");

        assertThat(response.available()).isTrue();
        verify(userRepository).existsByNickname("uniqueNickname");
    }

    @Test
    @DisplayName("유저정보 조회 실패 - 유저 없음")
    void tc6_get_user_info_fail_not_found() {
        given(userRepository.findById("nonExistentUserId"))
                .willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo("nonExistentUserId"))
                .isInstanceOf(com.teamexp.learnflowapi.user.exception.UserNotFoundException.class);

        verify(userRepository).findById("nonExistentUserId");
    }

    @Test
    @DisplayName("유저정보 조회 성공 - 응답 매핑")
    void tc7_get_user_info_success() {
        String userId = "user-uuid-1";
        User user = User.createUser("test@test.com", "test1234", "nickname1", UserRole.MEMBER);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(membershipRepository.findByUserId(Mockito.any()))
                .willReturn(Optional.empty());

        var response = userService.getUserInfo(userId);

        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.nickname()).isEqualTo("nickname1");
        assertThat(response.role()).isEqualTo("MEMBER");

        verify(userRepository).findById(userId);
        verify(membershipRepository).findByUserId(Mockito.any());
    }



    @Test
    @DisplayName("회원탈퇴 - deleteById 호출")
    void tc8_withdraw_user_calls_deleteById() {
        String userId = "testUserId";

        userService.withdrawUser(userId);

        verify(userRepository).deleteById(userId);
    }
}
