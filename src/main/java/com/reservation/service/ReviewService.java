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

	/**
	 * 리뷰를 생성하는 메소드
	 * - 사용자의 예약 이력을 확인한 뒤, 리뷰와 이미지 정보를 함께 저장합니다.
	 * - S3에 이미지 업로드 후, DB에 해당 이미지 경로를 저장합니다.
	 *
	 * @param userId 리뷰를 작성하는 사용자 ID
	 * @param request 리뷰 요청 정보 (예약 ID, 평점, 내용)
	 * @param images 첨부된 이미지 리스트 (MultipartFile)
	 * @return 생성된 리뷰 응답 객체 (리뷰 정보 + 이미지 URL 목록)
	 * @throws IOException 이미지 업로드 실패 시
	 * @throws ReservationException 예약 정보가 없거나, 본인의 예약이 아닌 경우
	 */
	@Transactional
	public CreateReview.Response createReview(Long userId, CreateReview.Request request, List<MultipartFile> images)
			throws IOException {

		Reservation reservation = reservationRepository.findById(request.getReservationId())
				.orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

		if (!reservation.getUser().getId().equals(userId)) {
			throw new ReservationException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
		}

		Review review = Review.builder()
				.reservation(reservation)
				.user(reservation.getUser())
				.store(reservation.getStore())
				.rating(request.getRating())
				.content(request.getContent())
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();

		Review savedReview = reviewRepository.save(review);

		// 이미지 업로드 및 저장
		List<String> imageUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			for (MultipartFile image : images) {
				String imageUrl = s3UploaderService.upload(image, "reviews");
				imageUrls.add(imageUrl);
				reviewImageRepository.save(ReviewImage.builder()
						.review(savedReview)
						.imageUrl(imageUrl)
						.build());
			}
		}
		return CreateReview.Response.fromEntity(savedReview, imageUrls);
	}

	/**
	 * 리뷰를 삭제하는 메소드
	 * - 사용자가 작성한 리뷰만 삭제 가능
	 * - 리뷰에 연결된 이미지들도 S3 및 DB에서 함께 삭제됩니다.
	 *
	 * @param reviewId 삭제할 리뷰 ID
	 * @param userId 요청 사용자 ID
	 * @throws ReviewException 리뷰가 존재하지 않거나 권한이 없는 경우
	 */
	@Transactional
	public void deleteReview(Long reviewId, Long userId) {
	    Review review = reviewRepository.findById(reviewId)
	            .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

	    if (!review.getUser().getId().equals(userId)) {
	        throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
	    }

	    // 이미지 S3 및 DB 삭제
	    List<ReviewImage> images = reviewImageRepository.findByReviewId(reviewId);
	    for (ReviewImage image : images) {
	        String fileName = extractFileNameFromUrl(image.getImageUrl());
	        s3UploaderService.delete(fileName);
	        reviewImageRepository.delete(image);
	    }

	    // 리뷰 삭제
	    reviewRepository.delete(review);
	}

	/**
	 * 리뷰를 수정하는 메소드
	 * - 본인의 리뷰만 수정 가능
	 * - 리뷰 내용 및 평점 변경 가능
	 * - 기존 이미지는 모두 삭제하고 새 이미지로 대체
	 *
	 * @param reviewId 수정할 리뷰 ID
	 * @param userId 요청 사용자 ID
	 * @param newRating 새 평점
	 * @param newContent 새 리뷰 내용
	 * @param newImages 새 이미지 리스트 (MultipartFile)
	 * @throws IOException 이미지 업로드 실패 시
	 * @throws ReviewException 리뷰가 없거나 권한이 없는 경우
	 */
	@Transactional
	public void updateReview(Long reviewId, Long userId, int newRating, String newContent, List<MultipartFile> newImages)
			throws IOException {

	    Review review = reviewRepository.findById(reviewId)
	            .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));

	    if (!review.getUser().getId().equals(userId)) {
	        throw new ReviewException(ErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
	    }

	    // 리뷰 내용 수정
	    review.setRating(newRating);
	    review.setContent(newContent);
	    review.setUpdatedAt(LocalDateTime.now());

	    // 기존 이미지 삭제
	    List<ReviewImage> existingImages = reviewImageRepository.findByReviewId(reviewId);
	    for (ReviewImage image : existingImages) {
	        String fileName = extractFileNameFromUrl(image.getImageUrl());
	        s3UploaderService.delete(fileName);
	        reviewImageRepository.delete(image);
	    }

	    // 새 이미지 업로드 및 저장
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

	/**
	 * 이미지 URL에서 S3 경로만 추출하는 내부 유틸 메소드
	 * - 예: https://bucket.s3/reviews/img.jpg → reviews/img.jpg
	 *
	 * @param imageUrl 전체 이미지 URL
	 * @return 파일 경로 문자열
	 */
	private String extractFileNameFromUrl(String imageUrl) {
	    return imageUrl.substring(imageUrl.indexOf("reviews/"));
	}
}
