package com.reservation.dto.store;

import com.reservation.domain.Store;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class RegisterStore {

    /** 클라이언트에서 매장 등록 요청 시 사용하는 DTO */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotBlank(message = "매장 이름은 필수 입력 항목입니다.")
        private String storeName;

        private Double lat;

        private Double lng;

        private String detail;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {

        private Long storeId;
        private String storeName;
        private Double lat;
        private Double lng;
        private String detail;
        private Long ownerId;

        public static Response fromEntity(Store store) {
            return Response.builder()
                    .storeId(store.getId())
                    .storeName(store.getStoreName())
                    .lat(store.getLat())
                    .lng(store.getLng())
                    .detail(store.getDetail())
                    .ownerId(store.getOwner().getId())
                    .build();
        }
    }
}
