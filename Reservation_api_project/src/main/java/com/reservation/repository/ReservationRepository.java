package com.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.Reservation;
import com.reservation.type.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	List<Reservation> findByUserId(Long userId);
	
	// CANCELED 상태 제외한 예약만 조회하는 쿼리
	List<Reservation> findByUserIdAndStatusNot(Long userId, ReservationStatus status);

	// 중복되는 예약이 존재하는지 확인하는 쿼리
	boolean existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
		    Long userId, Long storeId, LocalDateTime reservationTime, ReservationStatus status);

	// 점주 아이디 및 예약 상태별 조회
	List<Reservation> findByStoreOwnerIdAndStatus(Long ownerId, ReservationStatus status);
	
	// 관리자용 전체 예약 상태 필터링
	List<Reservation> findByStatus(ReservationStatus status);



}