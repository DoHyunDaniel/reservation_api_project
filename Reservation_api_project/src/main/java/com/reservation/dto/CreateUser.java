package com.reservation.dto;

import java.time.LocalDateTime;

import com.reservation.type.UserType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CreateUser {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
	    @NotBlank
	    @Size(min = 5, max = 20, message = "아이디는 5~20자로 입력하세요.")
	    private String userId;
	    
	    @NotBlank
	    @Size(min = 1, max = 10, message = "닉네임은 1~10자로 입력하세요.")
	    private String nickname;
	    
	    @NotBlank
	    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	    private String password;
	    
	    @NotBlank
	    @Email(message = "이메일 형식이 올바르지 않습니다.")
	    private String email;
	    
	    @NotNull
	    private UserType userType;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private String userId;
		private String nickname;
		private String email;
		private UserType userType;
		private LocalDateTime createdAt;

		public static Response from(UserDto userDto) {
			return Response.builder()
					.userId(userDto.getUserId())
					.nickname(userDto.getNickname())
					.userType(userDto.getUserType())
					.email(userDto.getEmail())
					.createdAt(userDto.getCreatedAt())
					.build();
		}
	}
}
