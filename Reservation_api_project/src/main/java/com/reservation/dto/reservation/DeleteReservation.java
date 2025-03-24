package com.reservation.dto.reservation;

import lombok.*;

public class DeleteReservation {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long reservationId;
    }

    @Getter
    @Builder
    public static class Response {
        private Long reservationId;
        private String message;

        public static Response from(Long id) {
            return Response.builder()
                    .reservationId(id)
                    .message("예약이 정상적으로 삭제되었습니다.")
                    .build();
        }
    }
}
