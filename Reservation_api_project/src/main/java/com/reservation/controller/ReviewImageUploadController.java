package com.reservation.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.service.ImageUploadService;
import com.reservation.service.S3UploaderService;

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
}

