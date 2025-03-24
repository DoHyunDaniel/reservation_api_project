package com.reservation.dto.review;

import java.time.LocalDateTime;
import java.util.List;

import com.reservation.domain.Review;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class CreateReview {
    @Getter
    @Setter
    public static class Request {
        private Long reservationId;
        private Long storeId;
        private int rating;
        private String content;
        private List<String> imageUrls; // 리뷰 이미지 엔티티
    }

    @Getter 
    @Setter 
    @Builder
    public static class Response {
        private Long id;
        private Long reservationId;
        private Long storeId;
        private int rating;
        private String content;
        private LocalDateTime createdAt;
        private List<String> imageUrls;

        public static Response fromEntity(Review review, List<String> imageUrls) {
            return Response.builder()
                    .id(review.getId())
                    .reservationId(review.getReservation().getId())
                    .storeId(review.getStore().getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .createdAt(review.getCreatedAt())
                    .imageUrls(imageUrls)
                    .build();
        }
    }
}
