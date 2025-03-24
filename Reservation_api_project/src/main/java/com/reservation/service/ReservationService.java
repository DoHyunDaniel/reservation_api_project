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

	/**
	 * 예약 생성 메소드
	 * - 동일한 사용자/가게/시간의 예약이 존재하지 않을 경우 예약을 생성합니다.
	 * - 기본 상태는 PENDING(대기)입니다.
	 *
	 * @param userId 예약 요청 사용자 ID
	 * @param request 예약 요청 객체 (가게 ID, 시간, 전화번호 등)
	 * @return 생성된 예약 정보 응답
	 * @throws UserException 중복 예약, 사용자 또는 가게 미존재
	 */
	@Transactional
	public CreateReservation.Response createReservation(Long userId, CreateReservation.Request request) {
		boolean isDuplicate = reservationRepository.existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
	            userId, request.getStoreId(), request.getReservationTime(), ReservationStatus.CANCELED
	    );

	    if (isDuplicate) {
	        throw new UserException(ErrorCode.DUPLICATE_RESERVATION);
	    }

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

		Store store = storeRepository.findById(request.getStoreId())
				.orElseThrow(() -> new UserException(ErrorCode.STORE_NOT_FOUND));

		Reservation reservation = Reservation.builder()
				.user(user)
				.store(store)
				.reservationTime(request.getReservationTime())
				.phoneNumber(request.getPhoneNumber())
				.status(ReservationStatus.PENDING)
				.createdAt(LocalDateTime.now())
				.build();

		return CreateReservation.Response.fromEntity(reservationRepository.save(reservation));
	}

	/**
	 * 사용자 ID로 예약 목록 조회
	 * - 취소된 예약(CANCELED)은 제외합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 예약 목록
	 */
	@Transactional(readOnly = true)
	public List<ReservationDto> getReservationsByUserId(Long userId) {
	    List<Reservation> reservations = reservationRepository.findByUserId(userId).stream()
	            .filter(reservation -> reservation.getStatus() != ReservationStatus.CANCELED)
	            .toList();

	    return reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .collect(Collectors.toList());
	}

	/**
	 * 예약을 완전히 삭제하는 메소드 (Hard Delete)
	 * - 사용자 본인만 삭제 가능
	 *
	 * @param userId 요청 사용자 ID
	 * @param reservationId 삭제할 예약 ID
	 * @return 삭제된 예약 ID 응답
	 * @throws UserException 예약 미존재 또는 권한 없음
	 */
	@Transactional
	public DeleteReservation.Response deleteReservation(Long userId, Long reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new UserException(ErrorCode.RESERVATION_NOT_FOUND));

	    if (!reservation.getUser().getId().equals(userId)) {
	        throw new UserException(ErrorCode.INVALID_ROLE);
	    }

	    reservationRepository.delete(reservation);

	    return DeleteReservation.Response.from(reservationId);
	}

	/**
	 * 예약을 취소하는 메소드 (Soft Delete)
	 * - 상태만 CANCELED로 변경합니다.
	 * - 사용자 본인만 취소 가능
	 *
	 * @param userId 요청 사용자 ID
	 * @param reservationId 취소할 예약 ID
	 * @return 취소된 예약 ID 응답
	 */
	@Transactional
	public DeleteReservation.Response cancelReservation(Long userId, Long reservationId) {
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new UserException(ErrorCode.RESERVATION_NOT_FOUND));

	    if (!reservation.getUser().getId().equals(userId)) {
	        throw new UserException(ErrorCode.INVALID_ROLE);
	    }

	    reservation.setStatus(ReservationStatus.CANCELED);
	    return DeleteReservation.Response.from(reservationId);
	}

	/**
	 * 점주가 예약 상태를 변경하는 메소드
	 * - PENDING 상태인 예약만 변경 가능
	 * - 점주 본인의 매장 예약만 변경 가능
	 *
	 * @param ownerId 점주 ID
	 * @param request 예약 상태 변경 요청
	 * @return 변경된 예약 상태 응답
	 */
	@Transactional
	public ConfirmReservation.Response confirmReservation(Long ownerId, ConfirmReservation.Request request) {
	    Reservation reservation = reservationRepository.findById(request.getReservationId())
	            .orElseThrow(() -> new ReservationException(ErrorCode.RESERVATION_NOT_FOUND));

	    Store store = reservation.getStore();

	    if (!store.getOwner().getId().equals(ownerId)) {
	        throw new ReservationException(ErrorCode.UNAUTHORIZED);
	    }

	    if (reservation.getStatus() != ReservationStatus.PENDING) {
	        throw new ReservationException(ErrorCode.INVALID_RESERVATION_STATUS);
	    }

	    reservation.setStatus(request.getStatus());

	    return ConfirmReservation.Response.builder()
	            .reservationId(reservation.getId())
	            .status(reservation.getStatus())
	            .build();
	}

	/**
	 * 사용자의 체크인(도착) 처리를 수행하는 메소드
	 * - 예약 시간 기준 ±10분 이내 도착 시 체크인 가능
	 * - 상태가 REJECTED, CANCELED, CHECKED_IN인 경우 체크인 불가
	 *
	 * @param userId 사용자 ID
	 * @param reservationId 체크인할 예약 ID
	 * @throws ReservationException 조건 불충족 시 예외 발생
	 */
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

	/**
	 * 점주가 가진 매장 중 PENDING 상태의 예약 목록 조회
	 *
	 * @param ownerId 점주 ID
	 * @return PENDING 상태의 예약 목록
	 */
	public List<ReservationDto> getPendingReservationsForOwner(Long ownerId) {
	    List<Reservation> reservations = reservationRepository.findByStoreOwnerIdAndStatus(ownerId, ReservationStatus.PENDING);
	    return reservations.stream().map(ReservationDto::fromEntity).toList();
	}

	/**
	 * 점주가 예약 상태별로 예약 목록을 조회
	 *
	 * @param ownerId 점주 ID
	 * @param status 조회할 예약 상태
	 * @return 해당 상태의 예약 목록
	 */
	public List<ReservationDto> getReservationsByStatusForOwner(Long ownerId, ReservationStatus status) {
	    List<Reservation> reservations = reservationRepository.findByStoreOwnerIdAndStatus(ownerId, status);
	    return reservations.stream().map(ReservationDto::fromEntity).toList();
	}

	/**
	 * 모든 예약 중 특정 상태만 필터링하여 조회
	 *
	 * @param status 조회할 예약 상태
	 * @return 해당 상태의 예약 목록
	 */
	public List<ReservationDto> getAllReservationsByStatus(ReservationStatus status) {
	    List<Reservation> reservations = reservationRepository.findByStatus(status);
	    return reservations.stream().map(ReservationDto::fromEntity).toList();
	}
}
