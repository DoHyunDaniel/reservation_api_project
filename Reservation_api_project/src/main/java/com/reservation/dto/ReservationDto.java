package com.reservation.dto;

import java.time.LocalDateTime;

import com.reservation.domain.Reservation;
import com.reservation.type.ReservationStatus;
import com.reservation.type.UserType;

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
public class ReservationDto {
	private Long id;
	private Long userId;
	private Long storeId;
    private LocalDateTime reservationTime;
	private LocalDateTime createdAt;
	private ReservationStatus status;
	private String phoneNumber;
	
	public static ReservationDto fromEntity(Reservation reservation) {
		return ReservationDto.builder()
				.id(reservation.getId())
				.userId(reservation.getUser().getId())
				.storeId(reservation.getStore().getId())
				.reservationTime(reservation.getReservationTime())
				.createdAt(reservation.getCreatedAt())
				.status(reservation.getStatus())
				.phoneNumber(reservation.getPhoneNumber())
				.build();
	}
}
