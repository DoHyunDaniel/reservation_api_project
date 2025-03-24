package com.reservation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.service.ImageUploadService;
import com.reservation.service.ReviewService;
import com.reservation.service.S3UploaderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

// 리뷰 이미지 업로드를 위한 컨트롤러
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class ReviewImageUploadController {
    private final ImageUploadService imageUploadService;
    private final S3UploaderService s3Uploader;
    
    // 로컬에서 구현하기 위한 코드
//    @PostMapping("/review-image")
//    public ResponseEntity<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
//        String imageUrl = imageUploadService.uploadImage(file);
//        Map<String, String> response = new HashMap<>();
//        response.put("imageUrl", imageUrl);
//        return ResponseEntity.ok(response);
//    }
    
    // S3로 구현하기 위한 코드
    @PostMapping("/review-image")
    public ResponseEntity<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = s3Uploader.upload(file, "reviews");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.ok(response);
    }
    
    // 리뷰이미지 삭제 메소드
    @DeleteMapping("/review-image/{id}")
    public ResponseEntity<?> deleteReviewImage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        imageUploadService.deleteReviewImage(id, userId);
        return ResponseEntity.ok().build();
    }

    // 리뷰 이미지 업데이트 메소드
    @PutMapping("/review-image/{id}")
    public ResponseEntity<?> updateReviewImage(@PathVariable Long id,
                                               @RequestParam("file") MultipartFile newFile,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        imageUploadService.updateReviewImage(id, newFile, userId);
        return ResponseEntity.ok().build();
    }

    
}

