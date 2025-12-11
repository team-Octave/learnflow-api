package com.teamexp.learnflowapi.user.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.user.controller.dto.NicknameCheckResponse;
import com.teamexp.learnflowapi.user.controller.dto.UserCreateRequest;
import com.teamexp.learnflowapi.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.ok(null));
    }

    @GetMapping("/check")
    public ResponseEntity<BaseResponse<NicknameCheckResponse>> checkNickname(@RequestParam(name = "nickname") @NotBlank String nickname) {
        NicknameCheckResponse response = userService.checkNickname(nickname);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<Void>> deleteUser(@AuthenticationPrincipal CustomUserPrincipal principal) {
        userService.withdrawUser(principal.getId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.ok(null));
    }
}
