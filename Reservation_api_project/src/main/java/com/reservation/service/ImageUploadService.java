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

    // 로컬 저장 경로 (웹 접근 경로와는 다름)
    private final String uploadDir = "/uploads/reviews";

    private final ReviewImageRepository reviewImageRepository;
    private final S3UploaderService s3UploaderService;

    /**
     * MultipartFile 이미지를 로컬 서버에 업로드하는 메소드
     * - UUID 기반으로 파일명을 생성하고 지정된 폴더에 저장합니다.
     * - 프론트엔드에서 접근 가능한 URL 경로를 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 접근 가능한 이미지 경로 (예: "/images/reviews/uuid_filename.jpg")
     * @throws RuntimeException 업로드 중 IOException 발생 시
     */
    public String uploadImage(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            File saveFile = new File(uploadDir, fileName);

            // 디렉토리 생성
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }

            // 파일 저장
            file.transferTo(saveFile);

            return "/images/reviews/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }

    /**
     * 리뷰 이미지 하나를 삭제하는 메소드
     * - S3와 DB 양쪽에서 삭제 수행
     * - 사용자 본인의 리뷰 이미지에 대해서만 삭제 가능
     *
     * @param reviewImageId 삭제할 이미지 ID
     * @param userId 요청한 사용자 ID
     * @throws ReviewException 권한 없음 또는 이미지가 존재하지 않을 경우
     */
    @Transactional
    public void deleteReviewImage(Long reviewImageId, Long userId) {
        ReviewImage image = reviewImageRepository.findById(reviewImageId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_IMAGE_NOT_FOUND));

        if (!image.getReview().getUser().getId().equals(userId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        String fileName = extractFileNameFromUrl(image.getImageUrl());
        s3UploaderService.delete(fileName);

        reviewImageRepository.delete(image);
    }

    /**
     * 기존 이미지 URL에서 파일 경로만 추출하는 내부 유틸 메소드
     * - 예: https://bucket.s3/reviews/abc.jpg → reviews/abc.jpg
     *
     * @param imageUrl 전체 URL
     * @return S3 파일 경로
     */
    private String extractFileNameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.indexOf("reviews/"));
    }

    /**
     * 리뷰 이미지를 새 이미지로 교체하는 메소드
     * - 기존 이미지를 S3에서 삭제하고 새 이미지를 업로드합니다.
     * - 해당 이미지의 DB 정보는 URL만 업데이트됩니다.
     *
     * @param reviewImageId 수정할 이미지 ID
     * @param newFile 새로 업로드할 파일
     * @param userId 요청 사용자 ID
     * @throws ReviewException 권한 없음 또는 이미지 미존재
     */
    @Transactional
    public void updateReviewImage(Long reviewImageId, MultipartFile newFile, Long userId) {
        ReviewImage image = reviewImageRepository.findById(reviewImageId)
                .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_IMAGE_NOT_FOUND));

        if (!image.getReview().getUser().getId().equals(userId)) {
            throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 기존 이미지 삭제
        String oldFileName = extractFileNameFromUrl(image.getImageUrl());
        s3UploaderService.delete(oldFileName);

        // 새 이미지 업로드 및 DB 업데이트
        String newImageUrl = s3UploaderService.upload(newFile, "reviews");
        image.setImageUrl(newImageUrl);
    }
}
