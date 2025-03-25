package com.reservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.domain.Reservation;
import com.reservation.dto.ReservationDto;
import com.reservation.dto.reservation.ConfirmReservation;
import com.reservation.dto.reservation.CreateReservation;
import com.reservation.dto.reservation.DeleteReservation;
import com.reservation.repository.ReservationRepository;
import com.reservation.service.ReservationService;
import com.reservation.type.ReservationStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

	private final ReservationService reservationService;
	private final ReservationRepository reservationRepository;

	/**
	 * 일반 사용자가 예약을 생성하는 API
	 * - 예약 시간, 가게 ID, 전화번호 등을 포함
	 *
	 * @param request 예약 생성 요청
	 * @param httpRequest 사용자 인증 정보 포함
	 * @return 생성된 예약 정보
	 */
	@PostMapping("/reserve")
	public ResponseEntity<CreateReservation.Response> createReservation(
	        @RequestBody @Valid CreateReservation.Request request,
	        HttpServletRequest httpRequest) {

	    Long userId = (Long) httpRequest.getAttribute("userId");
	    CreateReservation.Response response = reservationService.createReservation(userId, request);
	    return ResponseEntity.ok(response);
	}

	/**
	 * 현재 로그인한 사용자의 예약 목록을 조회
	 * - 취소된 예약(CANCELED)은 제외됨
	 *
	 * @param request 인증 정보 포함
	 * @return 예약 목록
	 */
	@GetMapping("/my-reservations")
	public ResponseEntity<List<ReservationDto>> getMyReservations(HttpServletRequest request) {
	    Long userId = (Long) request.getAttribute("userId");
	    List<Reservation> reservations = reservationRepository.findByUserIdAndStatusNot(userId, ReservationStatus.CANCELED);

	    List<ReservationDto> result = reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .toList();

	    return ResponseEntity.ok(result);
	}

	/**
	 * 예약을 완전히 삭제하는 API (Hard Delete)
	 *
	 * @param request 예약 ID 포함
	 * @param httpRequest 인증 정보 포함
	 * @return 삭제된 예약 ID
	 */
	@DeleteMapping("/delete")
	public ResponseEntity<DeleteReservation.Response> deleteReservation(
	        @RequestBody DeleteReservation.Request request,
	        HttpServletRequest httpRequest
	) {
	    Long userId = (Long) httpRequest.getAttribute("userId");
	    DeleteReservation.Response response = reservationService.deleteReservation(userId, request.getReservationId());
	    return ResponseEntity.ok(response);
	}

	/**
	 * 예약을 취소하는 API (Soft Delete)
	 * - 상태만 CANCELED로 변경
	 *
	 * @param request 예약 ID 포함
	 * @param httpRequest 인증 정보 포함
	 * @return 취소된 예약 ID
	 */
	@PutMapping("/cancel")
	public ResponseEntity<DeleteReservation.Response> cancelReservation(
	        @RequestBody DeleteReservation.Request request,
	        HttpServletRequest httpRequest
	) {
	    Long userId = (Long) httpRequest.getAttribute("userId");
	    DeleteReservation.Response response = reservationService.cancelReservation(userId, request.getReservationId());
	    return ResponseEntity.ok(response);
	}

	/**
	 * 점주가 PENDING(승인 대기중) 상태의 예약 목록을 조회
	 *
	 * @param request 인증 정보 포함 (점주)
	 * @return 대기 중인 예약 목록
	 */
	@GetMapping("/owner/pending")
	public ResponseEntity<List<ReservationDto>> getPendingReservationsForOwner(HttpServletRequest request) {
	    Long ownerId = (Long) request.getAttribute("userId");
	    List<ReservationDto> reservations = reservationService.getPendingReservationsForOwner(ownerId);
	    return ResponseEntity.ok(reservations);
	}

	/**
	 * 점주가 예약을 승인 또는 거절하는 기능
	 *
	 * @param request 예약 ID 및 새 상태 포함
	 * @param httpRequest 인증 정보 포함 (OWNER 권한 필요)
	 * @return 업데이트된 예약 상태
	 */
	@PutMapping("/confirm")
	public ResponseEntity<ConfirmReservation.Response> confirmReservation(
	        @Valid @RequestBody ConfirmReservation.Request request,
	        HttpServletRequest httpRequest
	) {
		String role = (String)httpRequest.getAttribute("role");
		if(!role.equals("OWNER")) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

	    Long ownerId = (Long) httpRequest.getAttribute("userId");
	    ConfirmReservation.Response response = reservationService.confirmReservation(ownerId, request);
	    return ResponseEntity.ok(response);
	}

	/**
	 * 점주 또는 사용자 본인이 체크인(방문 확인)하는 기능
	 * - 예약 시간 ±10분 이내일 때만 가능
	 *
	 * @param reservationId 체크인할 예약 ID
	 * @param request 인증 정보 포함
	 * @return 성공 메시지
	 */
	@PutMapping("/check-in/{reservationId}")
	public ResponseEntity<?> checkInReservation(@PathVariable Long reservationId, HttpServletRequest request) {
	    Long userId = (Long) request.getAttribute("userId");
	    reservationService.checkInReservation(userId, reservationId);
	    return ResponseEntity.ok("체크인 완료");
	}

	/**
	 * 관리자: 전체 예약 목록 조회
	 *
	 * @param httpRequest 인증 정보 포함 (ADMIN 권한)
	 * @return 모든 예약 목록
	 */
	@GetMapping("/admin/reservations")
	public ResponseEntity<List<ReservationDto>> getAllReservationsForAdmin(HttpServletRequest httpRequest) {
	    String role = (String) httpRequest.getAttribute("role");
	    if (!"ADMIN".equals(role)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	    }

	    List<Reservation> reservations = reservationRepository.findAll();

	    List<ReservationDto> result = reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .toList();

	    return ResponseEntity.ok(result);
	}

	/**
	 * 점주가 특정 상태의 예약 목록을 조회
	 *
	 * @param status 필터링할 예약 상태 (예: APPROVED)
	 * @param request 인증 정보 포함 (점주)
	 * @return 해당 상태의 예약 목록
	 */
	@GetMapping("/owner/status")
	public ResponseEntity<List<ReservationDto>> getReservationsByStatusForOwner(
	        @RequestParam("status") ReservationStatus status,
	        HttpServletRequest request) {

	    Long ownerId = (Long) request.getAttribute("userId");
	    List<ReservationDto> reservations = reservationService.getReservationsByStatusForOwner(ownerId, status);
	    return ResponseEntity.ok(reservations);
	}

	/**
	 * 관리자: 전체 예약 중 특정 상태의 예약만 조회
	 *
	 * @param status 예약 상태 (예: CANCELED)
	 * @return 필터링된 예약 목록
	 */
	@GetMapping("/admin/status")
	public ResponseEntity<List<ReservationDto>> getAllReservationsByStatus(@RequestParam("status") ReservationStatus status) {
	    List<ReservationDto> reservations = reservationService.getAllReservationsByStatus(status);
	    return ResponseEntity.ok(reservations);
	}
}
