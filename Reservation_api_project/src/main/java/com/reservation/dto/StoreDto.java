package com.reservation.dto;

import java.time.LocalDateTime;

import com.reservation.domain.Store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreDto {
	private Long id;
	private String storeName;
	private Double lat;
	private Double lng;
	private String detail;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Long ownerId;
	
	public static StoreDto fromEntity(Store store) {
		return StoreDto.builder()
				.id(store.getId())
				.storeName(store.getStoreName())
				.lat(store.getLat())
				.lng(store.getLng())
				.detail(store.getDetail())
				.createdAt(store.getCreatedAt())
				.updatedAt(store.getUpdatedAt())
				// 외래키
				.ownerId(store.getOwner().getId())
				
				.build();
	}
}
