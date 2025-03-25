package com.reservation.dto.reviewImage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class ReviewImageUploadDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<String> uploadedImageUrls;

        public static Response from(List<String> urls) {
            return Response.builder()
                    .uploadedImageUrls(urls)
                    .build();
        }
    }
}
