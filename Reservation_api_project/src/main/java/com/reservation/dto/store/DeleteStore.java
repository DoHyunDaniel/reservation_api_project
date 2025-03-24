package com.reservation.dto.store;

import java.time.LocalDateTime;

import com.reservation.dto.StoreDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class DeleteStore {
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request{
		@NotNull
        private Long id;
		
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
		private LocalDateTime deletedAt;

		public static Response from(StoreDto storeDto) {
			return Response.builder()
					.id(storeDto.getId())
					.deletedAt(LocalDateTime.now())
					.build();
		}

	}
}
