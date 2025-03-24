package com.reservation.dto.reservation;

import java.time.LocalDateTime;

import com.reservation.domain.Reservation;
import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.type.ReservationStatus;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CreateReservation {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "storeId는 필수입니다.")
        private Long storeId;

        @Future(message = "예약 시간은 미래 시각이어야 합니다.")
        @NotNull(message = "예약 시간은 필수입니다.")
        private LocalDateTime reservationTime;

        @NotBlank(message = "전화번호는 필수입니다.")
        private String phoneNumber;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long reservationId;
        private Long storeId;
        private Long userId;
        private String storeName;
        private LocalDateTime reservationTime;
        private ReservationStatus status;
        private String phoneNumber;
        private LocalDateTime createdAt;

        public static Response fromEntity(Reservation reservation) {
            return Response.builder()
                    .reservationId(reservation.getId())
                    .storeId(reservation.getStore().getId())
                    .userId(reservation.getUser().getId())
                    .storeName(reservation.getStore().getStoreName())
                    .reservationTime(reservation.getReservationTime())
                    .status(reservation.getStatus())
                    .phoneNumber(reservation.getUser().getPhoneNumber())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }
    }
}
