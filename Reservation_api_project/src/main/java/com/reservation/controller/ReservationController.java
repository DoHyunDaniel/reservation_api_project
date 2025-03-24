package com.reservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
	
	@PostMapping("/reserve")
	public ResponseEntity<CreateReservation.Response> createReservation(
	        @RequestBody @Valid CreateReservation.Request request,
	        HttpServletRequest httpRequest) {

	    Long userId = (Long) httpRequest.getAttribute("userId"); // 토큰 필터에서 저장된 유저 ID

	    CreateReservation.Response response = reservationService.createReservation(userId, request);
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/my-reservations")
	public ResponseEntity<List<ReservationDto>> getMyReservations(HttpServletRequest request) {
	    Long userId = (Long) request.getAttribute("userId");
	    List<Reservation> reservations = reservationRepository.findByUserIdAndStatusNot(userId, ReservationStatus.CANCELED);

	    List<ReservationDto> result = reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .toList();

	    return ResponseEntity.ok(result);
	}

	
	@DeleteMapping("/delete")
	public ResponseEntity<DeleteReservation.Response> deleteReservation(
	        @RequestBody DeleteReservation.Request request,
	        HttpServletRequest httpRequest
	) {
	    Long userId = (Long) httpRequest.getAttribute("userId");
	    DeleteReservation.Response response = reservationService.deleteReservation(userId, request.getReservationId());
	    return ResponseEntity.ok(response);
	}

	@PutMapping("/cancel")
	public ResponseEntity<DeleteReservation.Response> cancelReservation(
	        @RequestBody DeleteReservation.Request request,
	        HttpServletRequest httpRequest
	) {
	    Long userId = (Long) httpRequest.getAttribute("userId");
	    DeleteReservation.Response response = reservationService.cancelReservation(userId, request.getReservationId());
	    return ResponseEntity.ok(response);
	}
	
	// 점주가 "PENDING"인(승인대기중인) 예약들을 조회하는 기능
	@GetMapping("/owner/pending")
	public ResponseEntity<List<ReservationDto>> getPendingReservationsForOwner(HttpServletRequest request) {
	    Long ownerId = (Long) request.getAttribute("userId");

	    List<ReservationDto> reservations = reservationService.getPendingReservationsForOwner(ownerId);
	    return ResponseEntity.ok(reservations);
	}
	
	// 점주가 예약을 승인하는 기능
	@PutMapping("/confirm")
	public ResponseEntity<ConfirmReservation.Response> confirmReservation(
	        @Valid @RequestBody ConfirmReservation.Request request,
	        HttpServletRequest httpRequest
	) {
		String role = (String)httpRequest.getAttribute("role");
		if(!role.equals("OWNER")) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
		}
	    Long ownerId = (Long) httpRequest.getAttribute("userId");

	    ConfirmReservation.Response response = reservationService.confirmReservation(ownerId, request);
	    return ResponseEntity.ok(response);
	}

	
	// 관리자용 전체 조회 기능
	@GetMapping("/admin/reservations")
	public ResponseEntity<List<ReservationDto>> getAllReservationsForAdmin(HttpServletRequest httpRequest) {
	    String role = (String) httpRequest.getAttribute("role");
	    if (!"ADMIN".equals(role)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
	    }

	    List<Reservation> reservations = reservationRepository.findAll();

	    List<ReservationDto> result = reservations.stream()
	            .map(ReservationDto::fromEntity)
	            .toList();

	    return ResponseEntity.ok(result);
	}	
	
	// 점주용: 자신의 매장에 걸린 특정 상태의 예약
	// 예시: GET /reservations/owner/status?status=APPROVED 
	// Authorization: Bearer <점주 JWT>

	@GetMapping("/owner/status")
	public ResponseEntity<List<ReservationDto>> getReservationsByStatusForOwner(
	        @RequestParam("status") ReservationStatus status,
	        HttpServletRequest request) {
	    
	    Long ownerId = (Long) request.getAttribute("userId");
	    List<ReservationDto> reservations = reservationService.getReservationsByStatusForOwner(ownerId, status);
	    return ResponseEntity.ok(reservations);
	}

	// 관리자용: 전체 예약 중 특정 상태만 필터링
	// 예시: GET /reservations/admin/status?status=CANCELED
	// Authorization: Bearer <관리자 JWT>
	@GetMapping("/admin/status")
	public ResponseEntity<List<ReservationDto>> getAllReservationsByStatus(@RequestParam("status") ReservationStatus status) {
	    List<ReservationDto> reservations = reservationService.getAllReservationsByStatus(status);
	    return ResponseEntity.ok(reservations);
	}




}
