package com.reservation.dto.review;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class UpdateReview {

    @Getter
    @Setter
    public static class Request {
        private int rating;
        private String content;
        private List<MultipartFile> images;
    }

    @Getter
    @Setter
    public static class Response {
        private Long id;
        private int rating;
        private String content;
        private List<String> imageUrls;
    }
}
