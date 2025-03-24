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

	@PostMapping(value = "/create", consumes = { "multipart/form-data" })
	public ResponseEntity<CreateReview.Response> createReview(@RequestPart("request") CreateReview.Request request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images, HttpServletRequest httpRequest)
			throws IOException {

		Long userId = (Long) httpRequest.getAttribute("userId");
		CreateReview.Response response = reviewService.createReview(userId, request, images);
		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("/reviews/{reviewId}")
	public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpServletRequest request) {
	    Long userId = (Long) request.getAttribute("userId");
	    reviewService.deleteReview(reviewId, userId);
	    return ResponseEntity.ok("리뷰가 삭제되었습니다.");
	}

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
