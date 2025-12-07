package com.teamexp.learnflowapi.user.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.user.controller.dto.UserCreateRequest;
import com.teamexp.learnflowapi.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
