package com.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 리뷰를 저장하는 메소드
     * - 신규 리뷰 생성 또는 기존 리뷰 수정에 사용됩니다.
     * - JpaRepository에서 제공하는 기본 save 기능과 동일합니다.
     *
     * @param review 저장할 리뷰 엔티티
     * @return 저장된 리뷰 엔티티
     */
    Review save(Review review);
}
