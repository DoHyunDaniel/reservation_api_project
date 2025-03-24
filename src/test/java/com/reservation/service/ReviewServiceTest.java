package com.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.reservation.domain.*;
import com.reservation.dto.review.CreateReview;
import com.reservation.exception.ReservationException;
import com.reservation.exception.ReviewException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.ReviewImageRepository;
import com.reservation.repository.ReviewRepository;
import com.reservation.type.ErrorCode;
import com.reservation.type.ReservationStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReviewServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewImageRepository reviewImageRepository;
    @Mock private S3UploaderService s3UploaderService;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 리뷰 생성")
    void createReview_success() throws IOException {
        Long userId = 1L;
        Long reservationId = 10L;

        User user = User.builder().id(userId).build();
        Store store = Store.builder().id(100L).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(user)
                .store(store)
                .build();

        CreateReview.Request request = new CreateReview.Request();
        request.setReservationId(reservationId);
        request.setRating(4);
        request.setContent("좋아요!");

        Review review = Review.builder()
                .id(123L)
                .user(user)
                .store(store)
                .reservation(reservation)
                .rating(4)
                .content("좋아요!")
                .build();

        MultipartFile mockFile = mock(MultipartFile.class);
        String fakeImageUrl = "https://s3.bucket/reviews/sample.jpg";

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(s3UploaderService.upload(mockFile, "reviews")).thenReturn(fakeImageUrl);

        var response = reviewService.createReview(userId, request, List.of(mockFile));

        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getImageUrls()).contains(fakeImageUrl);
        verify(reviewImageRepository, times(1)).save(any(ReviewImage.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 다른 사람의 예약")
    void createReview_notOwner() {
        Long userId = 1L;
        Long reservationId = 10L;

        User otherUser = User.builder().id(99L).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(otherUser)
                .build();

        CreateReview.Request request = new CreateReview.Request();
        request.setReservationId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationException.class, () -> reviewService.createReview(userId, request, List.of()));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_success() {
        Long userId = 1L;
        Long reviewId = 5L;
        String imageUrl = "https://bucket.s3/reviews/file1.jpg";

        Review review = Review.builder().id(reviewId).user(User.builder().id(userId).build()).build();
        ReviewImage image = ReviewImage.builder().review(review).imageUrl(imageUrl).build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewImageRepository.findByReviewId(reviewId)).thenReturn(List.of(image));

        reviewService.deleteReview(reviewId, userId);

        verify(s3UploaderService).delete("reviews/file1.jpg");
        verify(reviewImageRepository).delete(image);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_success() throws IOException {
        Long userId = 1L;
        Long reviewId = 7L;
        String oldImageUrl = "https://bucket.s3/reviews/old.jpg";
        String newImageUrl = "https://bucket.s3/reviews/new.jpg";

        User user = User.builder().id(userId).build();
        Review review = Review.builder()
                .id(reviewId)
                .user(user)
                .rating(3)
                .content("old content")
                .updatedAt(LocalDateTime.now())
                .build();

        ReviewImage oldImage = ReviewImage.builder()
                .imageUrl(oldImageUrl)
                .review(review)
                .build();

        MultipartFile newFile = mock(MultipartFile.class);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewImageRepository.findByReviewId(reviewId)).thenReturn(List.of(oldImage));
        when(s3UploaderService.upload(newFile, "reviews")).thenReturn(newImageUrl);

        reviewService.updateReview(reviewId, userId, 5, "new content", List.of(newFile));

        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("new content");

        verify(s3UploaderService).delete("reviews/old.jpg");
        verify(reviewImageRepository).delete(oldImage);
        verify(reviewImageRepository).save(any(ReviewImage.class));
    }
}
