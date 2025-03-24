package com.reservation.service;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.domain.ReviewImage;
import com.reservation.exception.ReviewException;
import com.reservation.repository.ReviewImageRepository;
import com.reservation.type.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private final String uploadDir = "/uploads/reviews"; // 루트 디렉토리 기준 경로
    private final ReviewImageRepository reviewImageRepository;
    private final S3UploaderService s3UploaderService;
    
    public String uploadImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            File saveFile = new File(uploadDir, fileName);

            // 디렉토리 없으면 생성
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            file.transferTo(saveFile);

            // 프론트가 접근 가능한 URL 반환 (경로 설정에 따라 다를 수 있음)
            return "/images/reviews/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
    
    @Transactional
    public void deleteReviewImage(Long reviewImageId, Long userId) {
        ReviewImage image = reviewImageRepository.findById(reviewImageId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_IMAGE_NOT_FOUND));

        // 사용자 인증 확인 (옵션)
        if (!image.getReview().getUser().getId().equals(userId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // S3에서 삭제
        String fileName = extractFileNameFromUrl(image.getImageUrl());
        s3UploaderService.delete(fileName);

        // DB에서 삭제
        reviewImageRepository.delete(image);
    }

    private String extractFileNameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf("reviews/")); // 예: reviews/uuid_filename.jpg
    }
    
    
    // 이미지 수정
    // 1. 기존 이미지 삭제
    // 2. 새 이미지 업로드
    @Transactional
    public void updateReviewImage(Long reviewImageId, MultipartFile newFile, Long userId) {
        ReviewImage image = reviewImageRepository.findById(reviewImageId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_IMAGE_NOT_FOUND));

        if (!image.getReview().getUser().getId().equals(userId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 기존 이미지 S3에서 삭제
        String oldFileName = extractFileNameFromUrl(image.getImageUrl());
        s3UploaderService.delete(oldFileName);

        // 새 이미지 업로드
        String newImageUrl = s3UploaderService.upload(newFile, "reviews");
        image.setImageUrl(newImageUrl); // 업데이트
    }


}
