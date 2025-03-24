package com.reservation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.reservation.dto.review.CreateReview;
import com.reservation.service.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

	private final ReviewService reviewService;

	/**
	 * 리뷰 생성 API
	 * - 예약을 완료한 사용자가 리뷰와 이미지(선택)를 등록합니다.
	 * - multipart/form-data 형식으로 요청
	 *
	 * @param request 리뷰 작성 정보 (예약 ID, 평점, 내용 등)
	 * @param images 첨부 이미지 목록 (선택)
	 * @param httpRequest 사용자 인증 정보 포함 (userId)
	 * @return 생성된 리뷰 응답 DTO
	 * @throws IOException 이미지 업로드 실패 시
	 */
	@PostMapping(value = "/create", consumes = { "multipart/form-data" })
	public ResponseEntity<CreateReview.Response> createReview(
			@RequestPart("request") CreateReview.Request request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images,
			HttpServletRequest httpRequest) throws IOException {

		Long userId = (Long) httpRequest.getAttribute("userId");
		CreateReview.Response response = reviewService.createReview(userId, request, images);
		return ResponseEntity.ok(response);
	}

	/**
	 * 리뷰 삭제 API
	 * - 사용자가 본인이 작성한 리뷰를 삭제합니다.
	 * - 리뷰에 포함된 이미지도 함께 삭제됩니다 (S3 + DB)
	 *
	 * @param reviewId 삭제할 리뷰 ID
	 * @param request 사용자 인증 정보 포함 (userId)
	 * @return 삭제 완료 메시지
	 */
	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
	    Long userId = (Long) request.getAttribute("userId");
	    reviewService.deleteReview(reviewId, userId);
	    return ResponseEntity.ok("리뷰가 삭제되었습니다.");
	}

	/**
	 * 리뷰 수정 API
	 * - 기존 리뷰의 평점, 내용, 이미지 전체를 수정합니다.
	 * - 기존 이미지는 삭제 후 새 이미지로 대체됩니다.
	 * - multipart/form-data 형식으로 요청
	 *
	 * @param reviewId 수정할 리뷰 ID
	 * @param rating 수정할 평점
	 * @param content 수정할 내용
	 * @param images 새 이미지 파일 리스트 (선택)
	 * @param request 사용자 인증 정보 포함 (userId)
	 * @return 수정 완료 메시지
	 * @throws IOException 이미지 업로드 실패 시
	 */
	@PutMapping("/reviews/{reviewId}")
	public ResponseEntity<?> updateReview(
	        @PathVariable Long reviewId,
	        @RequestParam int rating,
	        @RequestParam String content,
	        @RequestPart(required = false) List<MultipartFile> images,
	        HttpServletRequest request) throws IOException {

	    Long userId = (Long) request.getAttribute("userId");
	    reviewService.updateReview(reviewId, userId, rating, content, images);
	    return ResponseEntity.ok("리뷰가 수정되었습니다.");
	}
}
