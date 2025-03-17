package com.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.config.JwtTokenProvider;
import com.reservation.dto.LoginRequest;
import com.reservation.dto.LoginResponse;
import com.reservation.service.UserService;

@RestController
@RequestMapping("/users/auth")
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    
    // 로그인시 jwt 토큰 제공
    public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    
    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var user = userService.validateUser(request.getEmail(), request.getPassword());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUserType().toString(), user.isPartner());
        
        return ResponseEntity.ok(LoginResponse.from(token, user.getId(), user.getUserType().toString()));
    }
}
