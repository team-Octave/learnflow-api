package com.teamexp.learnflowapi.auth.controller;

import com.teamexp.learnflowapi.auth.controller.dto.LoginRequest;
import com.teamexp.learnflowapi.auth.controller.dto.LoginResponse;
import com.teamexp.learnflowapi.auth.controller.dto.ReissuanceResponse;
import com.teamexp.learnflowapi.auth.service.AuthService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
    public ResponseEntity<BaseResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/reissue")
    public ResponseEntity<BaseResponse<ReissuanceResponse>> reissueToken(@CookieValue(name = "refresh_token") String refreshToken) {

        ReissuanceResponse response = authService.reissueToken(refreshToken);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
        @CookieValue(name = "refresh_token") String refreshToken
    ) {
        authService.logout(refreshToken);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(null));
    }




}
