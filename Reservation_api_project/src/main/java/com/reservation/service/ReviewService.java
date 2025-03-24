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

}
