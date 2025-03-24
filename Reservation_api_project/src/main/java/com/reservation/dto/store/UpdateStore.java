package com.reservation.dto.store;

import com.reservation.domain.Store;
import com.reservation.type.UserType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UpdateStore {

    /** 매장 수정 요청 DTO */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {

        @NotNull(message = "매장 ID는 필수입니다.")
        private Long id;

        @NotBlank(message = "매장 이름은 필수 입력 항목입니다.")
        private String storeName;

        private Double lat;

        private Double lng;

        private String detail;

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        private String password;

        @NotNull(message = "유저 타입은 필수 입력 항목입니다.")
        private UserType userType;
    }

    /** 매장 수정 응답 DTO */
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
