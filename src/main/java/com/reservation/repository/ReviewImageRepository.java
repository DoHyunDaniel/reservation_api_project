package com.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.ReviewImage;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 특정 리뷰에 연결된 모든 이미지 목록을 조회하는 메소드
     * - 리뷰 ID를 기준으로 해당 리뷰에 등록된 이미지들을 가져옵니다.
     * - 이미지 삭제, 수정, 출력 시 사용됩니다.
     *
     * @param reviewId 대상 리뷰 ID
     * @return 해당 리뷰에 연결된 이미지 리스트
     */
    List<ReviewImage> findByReviewId(Long reviewId);
}
