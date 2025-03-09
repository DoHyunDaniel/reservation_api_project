package com.reservation.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DeleteUser {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
	    @NotBlank
	    @Size(min = 5, max = 20)
	    private String userId;
	    
	    @NotBlank
	    @Size(min = 8)
	    private String password;
	}
	
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Response {
		private String userId;
		private LocalDateTime deletedAt;

		public static Response from(UserDto userDto) {
			return Response.builder()
					.userId(userDto.getUserId())
					.deletedAt(userDto.getCreatedAt())
					.build();
		}
	}
}
