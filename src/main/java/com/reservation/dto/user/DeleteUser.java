package com.reservation.dto.user;

import java.time.LocalDateTime;

import com.reservation.dto.UserDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
		@NotNull
        private Long id;
		
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
		private Long id;
		private String userId;
		private LocalDateTime deletedAt;

		public static Response from(UserDto userDto) {
			return Response.builder()
					.id(userDto.getId())
					.userId(userDto.getUserId())
					.deletedAt(userDto.getCreatedAt())
					.build();
		}
	}
}
