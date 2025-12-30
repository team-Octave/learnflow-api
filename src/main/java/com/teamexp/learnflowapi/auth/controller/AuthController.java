package com.teamexp.learnflowapi.auth.controller;

import com.teamexp.learnflowapi.auth.controller.dto.LoginRequest;
import com.teamexp.learnflowapi.auth.controller.dto.LoginResponse;
import com.teamexp.learnflowapi.auth.controller.dto.ReissuanceResponse;
import com.teamexp.learnflowapi.auth.service.AuthService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }

    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<ReissuanceResponse>> reissueToken(@RequestHeader(name = "Authorization") String tokenHeader) {

        ReissuanceResponse response = authService.reissueToken(tokenHeader);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }


}
