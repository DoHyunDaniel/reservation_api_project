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
		@NotNull
		private Long id;
		
	    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
	    @Size(min = 5, max = 20, message = "아이디는 5~20자로 입력하세요.")
	    private String userId;
	    
	    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
	    @Size(min = 1, max = 10, message = "닉네임은 1~10자로 입력하세요.")
	    private String nickname;
	    
	    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
	    private String password;
	    
	    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
	    @Email(message = "이메일 형식이 올바르지 않습니다.")
	    private String email;
	    
	    @NotNull(message = "가입자 유형은 필수 입력 항목입니다.")
	    private UserType userType;
	    
	    @NotNull(message = "전화번호는 필수 입력 항목입니다.")
	    @Size(max=20, message = "전화번호는 010-XXXX-XXXX 형식으로 입력해주세요.")
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
		private LocalDateTime createdAt;
		private String phoneNumber;

		public static Response from(UserDto userDto) {
			return Response.builder()
					.id(userDto.getId())
					.userId(userDto.getUserId())
					.nickname(userDto.getNickname())
					.userType(userDto.getUserType())
					.email(userDto.getEmail())
					.createdAt(userDto.getCreatedAt())
					.phoneNumber(userDto.getPhoneNumber())
					.build();
		}
	}
}
