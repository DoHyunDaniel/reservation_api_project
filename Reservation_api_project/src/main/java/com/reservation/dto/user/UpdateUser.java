package com.reservation.dto.user;

import java.time.LocalDateTime;

import com.reservation.domain.User;
import com.reservation.type.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UpdateUser {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;

        @NotBlank(message = "닉네임은 필수입니다.")
        private String nickname;

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        private String email;

        @NotNull(message = "유저 타입은 필수입니다.")
        private UserType userType;

        @NotBlank(message = "전화번호는 필수입니다.")
        private String phoneNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long id;
        private String userId;
        private String nickname;
        private String email;
        private UserType userType;
        private String phoneNumber;
        private LocalDateTime updatedAt;

        public static Response from(User user) {
            return Response.builder()
                    .id(user.getId())
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .userType(user.getUserType())
                    .phoneNumber(user.getPhoneNumber())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }
}
