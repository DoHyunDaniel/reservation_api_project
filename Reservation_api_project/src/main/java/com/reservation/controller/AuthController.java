package com.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reservation.config.JwtTokenProvider;
import com.reservation.dto.auth.LoginRequest;
import com.reservation.dto.auth.LoginResponse;
import com.reservation.service.UserService;

@RestController
@RequestMapping("/users/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 사용자 로그인 요청 처리
     * - 이메일과 비밀번호를 검증하고, 유효한 경우 JWT 토큰을 발급합니다.
     * - 로그인 성공 시 사용자 정보와 함께 토큰을 반환합니다.
     *
     * @param request 로그인 요청 객체 (이메일, 비밀번호)
     * @return JWT 토큰, 사용자 ID, 사용자 유형을 포함한 응답
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // 사용자 인증
        var user = userService.validateUser(request.getEmail(), request.getPassword());

        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getUserType().toString(),
                user.isPartner()
        );

        // 응답 반환
        return ResponseEntity.ok(LoginResponse.from(token, user.getId(), user.getUserType().toString()));
    }
}
