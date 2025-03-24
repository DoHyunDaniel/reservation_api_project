package com.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.Reservation;
import com.reservation.type.ReservationStatus;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 사용자의 모든 예약 목록을 조회
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 전체 예약 목록
     */
    List<Reservation> findByUserId(Long userId);

    /**
     * 특정 사용자의 예약 중 CANCELED 상태가 아닌 예약만 조회
     * - 주로 예약 중복 체크 또는 사용자 예약 목록 출력 시 사용
     *
     * @param userId 사용자 ID
     * @param status 제외할 상태 (보통 CANCELED)
     * @return 필터링된 예약 목록
     */
    List<Reservation> findByUserIdAndStatusNot(Long userId, ReservationStatus status);

    /**
     * 특정 사용자/가게/예약 시간 조건에 따라 중복 예약 여부 확인
     * - CANCELED가 아닌 상태에서 중복 확인
     *
     * @param userId 사용자 ID
     * @param storeId 매장 ID
     * @param reservationTime 예약 시간
     * @param status 제외할 상태
     * @return 중복 예약 존재 여부
     */
    boolean existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
            Long userId,
            Long storeId,
            LocalDateTime reservationTime,
            ReservationStatus status
    );

    /**
     * 점주가 소유한 매장의 예약 중 특정 상태의 예약 목록을 조회
     *
     * @param ownerId 점주 ID
     * @param status 예약 상태 (예: PENDING, CONFIRMED 등)
     * @return 해당 조건의 예약 목록
     */
    List<Reservation> findByStoreOwnerIdAndStatus(Long ownerId, ReservationStatus status);

    /**
     * 예약 상태별 전체 예약 목록을 조회 (관리자용)
     *
     * @param status 예약 상태
     * @return 전체 예약 중 해당 상태인 예약 목록
     */
    List<Reservation> findByStatus(ReservationStatus status);
}
