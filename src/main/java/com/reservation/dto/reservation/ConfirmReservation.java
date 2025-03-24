// ConfirmReservation.java
package com.reservation.dto.reservation;

import com.reservation.type.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class ConfirmReservation {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotNull
        private Long reservationId;

        @NotNull
        private ReservationStatus status; // APPROVED or REJECTED
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {
        private Long reservationId;
        private ReservationStatus status;
    }
}
