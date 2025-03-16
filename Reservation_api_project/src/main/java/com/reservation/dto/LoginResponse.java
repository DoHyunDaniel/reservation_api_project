package com.reservation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String role;

    public static LoginResponse from(String token, Long userId, String role) {
        return LoginResponse.builder()
                .token(token)
                .userId(userId)
                .role(role)
                .build();
    }
}
