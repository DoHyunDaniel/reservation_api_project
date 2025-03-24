package com.reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reservation.domain.Reservation;
import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.ReservationDto;
import com.reservation.dto.reservation.ConfirmReservation;
import com.reservation.dto.reservation.CreateReservation;
import com.reservation.dto.reservation.DeleteReservation;
import com.reservation.exception.ReservationException;
import com.reservation.exception.UserException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.ErrorCode;
import com.reservation.type.ReservationStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final StoreRepository storeRepository;
	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;

	@Transactional
	public CreateReservation.Response createReservation(Long userId, CreateReservation.Request request) {
		// 중복 예약 확인
	    boolean isDuplicate = reservationRepository.existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
	            userId, request.getStoreId(), request.getReservationTime(), ReservationStatus.CANCELED
	    );

	    if (isDuplicate) {
	        throw new UserException(ErrorCode.DUPLICATE_RESERVATION);
	    }
		User user = userRepository.findById(userId).orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		Store store = storeRepository.findById(request.getStoreId())
				.orElseThrow(() -> new UserException(ErrorCode.STORE_NOT_FOUND));

		Reservation reservation = Reservation.builder().user(user).store(store)
				.reservationTime(request.getReservationTime()).phoneNumber(request.getPhoneNumber())
				.status(ReservationStatus.PENDING).createdAt(LocalDateTime.now()).build();

		return CreateReservation.Response.fromEntity(reservationRepository.save(reservation));
	}

	
	// ID에 따라 예약 목록을 조회하는 메소드
	@Transactional(readOnly = true)
	public List<ReservationDto> getReservationsByUserId(Long userId) {
	    List<Reservation> reservations = reservationRepository.findByUserId(userId).stream()
	            .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELED) // 필터링
	            .toList();

	    return reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .collect(Collectors.toList());
	}

	
	// Hard-Delete로 예약 삭제
	@Transactional
	public DeleteReservation.Response deleteReservation(Long userId, Long reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new UserException(ErrorCode.RESERVATION_NOT_FOUND));

	    // 본인의 예약인지 확인
	    if (!reservation.getUser().getId().equals(userId)) {
	        throw new UserException(ErrorCode.INVALID_ROLE); // 권한 없음
	    }

	    reservationRepository.delete(reservation);

	    return DeleteReservation.Response.from(reservationId);
	}
	
	// Soft-Delete로 예약 취소
	@Transactional
	public DeleteReservation.Response cancelReservation(Long userId, Long reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new UserException(ErrorCode.RESERVATION_NOT_FOUND));

	    if (!reservation.getUser().getId().equals(userId)) {
	        throw new UserException(ErrorCode.INVALID_ROLE); // 본인 예약만 취소 가능
	    }

	    reservation.setStatus(ReservationStatus.CANCELED); // 상태만 변경
	    return DeleteReservation.Response.from(reservationId);
	}

	// 점주가 예약 확인 상태 변경하는 기능
	@Transactional
	public ConfirmReservation.Response confirmReservation(Long ownerId, ConfirmReservation.Request request) {
	    Reservation reservation = reservationRepository.findById(request.getReservationId())
	            .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

	    Store store = reservation.getStore();

	    if (!store.getOwner().getId().equals(ownerId)) {
	        throw new ReservationException(ErrorCode.UNAUTHORIZED); // 권한 없음
	    }

	    if (reservation.getStatus() != ReservationStatus.PENDING) {
	        throw new ReservationException(ErrorCode.INVALID_RESERVATION_STATUS); // 이미 처리된 예약
	    }

	    // 상태 변경
	    reservation.setStatus(request.getStatus());

	    return ConfirmReservation.Response.builder()
	            .reservationId(reservation.getId())
	            .status(reservation.getStatus())
	            .build();
	}
	
	
	// 점주가 방문을 확인하는 메소드
	@Transactional
	public void checkInReservation(Long userId, Long reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	        .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

	    if (!reservation.getUser().getId().equals(userId)) {
	        throw new ReservationException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
	    }

	    if (reservation.getStatus() == ReservationStatus.CHECKED_IN) {
	        throw new ReservationException(ErrorCode.ALREADY_CHECKED_IN);
	    }

	    if (reservation.getStatus() == ReservationStatus.REJECTED || reservation.getStatus() == ReservationStatus.CANCELED) {
	        throw new ReservationException(ErrorCode.INVALID_RESERVATION_STATUS);
	    }

	    // 예약 10분 전에 도착하면 에러 메세지
	    LocalDateTime now = LocalDateTime.now();
	    LocalDateTime reservationTime = reservation.getReservationTime();

	    LocalDateTime checkInStart = reservationTime.minusMinutes(10);
	    LocalDateTime checkInEnd = reservationTime.plusMinutes(10);

	    if (now.isBefore(checkInStart) || now.isAfter(checkInEnd)) {
	        throw new ReservationException(ErrorCode.NOT_IN_CHECKIN_WINDOW);
	    }


	    reservation.setStatus(ReservationStatus.CHECKED_IN);
	    reservation.setUpdatedAt(now);
	}

	
	// 상태가 'PENDING'인 예약만 조회
	public List<ReservationDto> getPendingReservationsForOwner(Long ownerId) {
	    List<Reservation> reservations = reservationRepository.findByStoreOwnerIdAndStatus(ownerId, ReservationStatus.PENDING);

	    return reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .toList();
	}
	
	// 예약 상태별 조회
	public List<ReservationDto> getReservationsByStatusForOwner(Long ownerId, ReservationStatus status) {
	    List<Reservation> reservations = reservationRepository.findByStoreOwnerIdAndStatus(ownerId, status);
	    return reservations.stream().map(ReservationDto::fromEntity).toList();
	}

	public List<ReservationDto> getAllReservationsByStatus(ReservationStatus status) {
	    List<Reservation> reservations = reservationRepository.findByStatus(status);
	    return reservations.stream().map(ReservationDto::fromEntity).toList();
	}




}
