package com.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.Reservation;
import com.reservation.domain.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Reservation, Long>{
	// 리뷰 저장 기능
	Review save(Review review);
	
}
