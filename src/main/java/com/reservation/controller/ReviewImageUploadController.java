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

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class ReviewImageUploadController {

    private final ImageUploadService imageUploadService;
    private final S3UploaderService s3Uploader;

    /**
     * 리뷰 이미지 업로드 (S3 저장 방식)
     * - 프론트에서 이미지 파일을 업로드하면 S3에 저장하고 URL을 반환합니다.
     * - 일반적으로 리뷰 작성 시 이미지 등록용으로 사용됩니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 업로드된 이미지의 S3 URL
     */
    @PostMapping("/review-image")
    public ResponseEntity<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = s3Uploader.upload(file, "reviews");

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.ok(response);
    }

    /**
     * 리뷰 이미지 삭제 API
     * - 사용자가 본인의 리뷰 이미지만 삭제할 수 있습니다.
     * - 이미지 파일은 S3에서도 삭제되며, DB에서도 제거됩니다.
     *
     * @param id 리뷰 이미지 ID
     * @param request 인증 정보 포함 (userId)
     * @return HTTP 200 OK 응답
     */
    @DeleteMapping("/review-image/{id}")
    public ResponseEntity<?> deleteReviewImage(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        imageUploadService.deleteReviewImage(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 리뷰 이미지 수정(교체) API
     * - 기존 이미지를 삭제한 뒤 새 이미지로 교체합니다.
     * - 이미지 URL은 DB에서 업데이트됩니다.
     *
     * @param id 리뷰 이미지 ID
     * @param newFile 새 이미지 파일
     * @param request 인증 정보 포함 (userId)
     * @return HTTP 200 OK 응답
     */
    @PutMapping("/review-image/{id}")
    public ResponseEntity<?> updateReviewImage(@PathVariable Long id,
                                               @RequestParam("file") MultipartFile newFile,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        imageUploadService.updateReviewImage(id, newFile, userId);
        return ResponseEntity.ok().build();
    }

    // 로컬 테스트용 업로드 코드 (비활성화)
    /*
    @PostMapping("/review-image")
    public ResponseEntity<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = imageUploadService.uploadImage(file);
        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        return ResponseEntity.ok(response);
    }
    */
}
