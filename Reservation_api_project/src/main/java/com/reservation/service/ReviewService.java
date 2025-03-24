package com.reservation.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.domain.Reservation;
import com.reservation.domain.Review;
import com.reservation.domain.ReviewImage;
import com.reservation.dto.review.CreateReview;
import com.reservation.exception.ReservationException;
import com.reservation.exception.ReviewException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.ReviewImageRepository;
import com.reservation.repository.ReviewRepository;
import com.reservation.type.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
	private final ReservationRepository reservationRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewImageRepository reviewImageRepository;
	private final S3UploaderService s3UploaderService;

	// 기본적인 리뷰 생성 로직
	@Transactional
	public CreateReview.Response createReview(Long userId, CreateReview.Request request, List<MultipartFile> images)
			throws IOException {
		// 예약 확인 로직
		Reservation reservation = reservationRepository.findById(request.getReservationId())
				.orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

		if (!reservation.getUser().getId().equals(userId)) {
			throw new ReservationException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
		}

		// 리뷰 생성
		Review review = Review.builder().reservation(reservation).user(reservation.getUser())
				.store(reservation.getStore()).rating(request.getRating()).content(request.getContent())
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

		Review savedReview = reviewRepository.save(review);

		// 리뷰 생성 후 이미지 저장
		List<String> imageUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			for (MultipartFile image : images) {
				// 저장 로직
				String imageUrl = s3UploaderService.upload(image, "reviews");
				imageUrls.add(imageUrl);
				reviewImageRepository.save(ReviewImage.builder().review(savedReview).imageUrl(imageUrl).build());
			}
		}
		return CreateReview.Response.fromEntity(savedReview, imageUrls);
	}

	// 리뷰 삭제 로직
	@Transactional
	public void deleteReview(Long reviewId, Long userId) {
	    Review review = reviewRepository.findById(reviewId)
	            .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

	    // 사용자 권한 확인
	    if (!review.getUser().getId().equals(userId)) {
	        throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
	    }

	    // 이미지 먼저 S3 및 DB에서 삭제
	    List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
	    for (ReviewImage image : images) {
	        String fileName = extractFileNameFromUrl(image.getImageUrl());
	        s3UploaderService.delete(fileName);
	        reviewImageRepository.delete(image);
	    }

	    // 리뷰 삭제
	    reviewRepository.delete(review);
	}

	private String extractFileNameFromUrl(String imageUrl) {
	    return imageUrl.substring(imageUrl.indexOf("reviews/")); // 경로만 추출
	}
	
	
	// 리뷰 수정 기능	
	@Transactional
	public void updateReview(Long reviewId, Long userId, int newRating, String newContent, List<MultipartFile> newImages) throws IOException {
	    Review review = reviewRepository.findById(reviewId)
	            .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

	    if (!review.getUser().getId().equals(userId)) {
	        throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
	    }

	    // 리뷰 내용 업데이트
	    review.setRating(newRating);
	    review.setContent(newContent);
	    review.setUpdatedAt(LocalDateTime.now());

	    // 기존 이미지 삭제 (S3 + DB)
	    List<ReviewImage> existingImages = reviewImageRepository.findByReviewId(reviewId);
	    for (ReviewImage image : existingImages) {
	        String fileName = extractFileNameFromUrl(image.getImageUrl());
	        s3UploaderService.delete(fileName);
	        reviewImageRepository.delete(image);
	    }

	    // 새 이미지 업로드
	    if (newImages != null) {
	        for (MultipartFile file : newImages) {
	            String newUrl = s3UploaderService.upload(file, "reviews");
				reviewImageRepository.save(ReviewImage.builder()
				        .review(review)
				        .imageUrl(newUrl)
				        .build());
	        }
	    }
	}



}
