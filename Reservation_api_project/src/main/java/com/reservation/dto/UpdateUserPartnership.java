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

public class UpdateUserPartnership {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
		@NotNull
		private Long id;

		@NotBlank
		@Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
		private String password;

		@NotNull
		private UserType userType;
		
		@NotNull
		private boolean isPartner;
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private Long id;
		private String password;
		private UserType userType;
		private LocalDateTime createdAt;
		private boolean isPartner;

		public static Response from(UserDto userDto) {
			return Response.builder().id(userDto.getId())
					.userType(userDto.getUserType()).isPartner(userDto.isPartner()).createdAt(userDto.getCreatedAt())
					.build();
		}

	}

}
