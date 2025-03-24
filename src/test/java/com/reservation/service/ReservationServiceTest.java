package com.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reservation.domain.Reservation;
import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.reservation.ConfirmReservation;
import com.reservation.dto.reservation.CreateReservation;
import com.reservation.exception.ReservationException;
import com.reservation.exception.UserException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.ReservationStatus;

public class ReservationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 예약 생성")
    void createReservation_success() {
        Long userId = 1L;
        Long storeId = 100L;

        CreateReservation.Request request = new CreateReservation.Request();
        request.setStoreId(storeId);
        request.setPhoneNumber("010-1111-2222");
        request.setReservationTime(LocalDateTime.now().plusHours(1));

        User user = User.builder().id(userId).build();
        Store store = Store.builder().id(storeId).build();
        Reservation reservation = Reservation.builder().user(user).store(store).reservationTime(request.getReservationTime()).build();

        when(reservationRepository.existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
                eq(userId), eq(storeId), eq(request.getReservationTime()), eq(ReservationStatus.CANCELED)
        )).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        var response = reservationService.createReservation(userId, request);

        assertThat(response.getReservationTime()).isEqualTo(request.getReservationTime());
    }

    @Test
    @DisplayName("중복 예약으로 예약 실패")
    void createReservation_duplicate() {
        Long userId = 1L;
        CreateReservation.Request request = new CreateReservation.Request();
        request.setStoreId(100L);
        request.setReservationTime(LocalDateTime.now().plusDays(1));

        when(reservationRepository.existsByUserIdAndStoreIdAndReservationTimeAndStatusNot(
                anyLong(), anyLong(), any(), eq(ReservationStatus.CANCELED)
        )).thenReturn(true);

        assertThrows(UserException.class, () -> reservationService.createReservation(userId, request));
    }

    @Test
    @DisplayName("예약 삭제 성공")
    void deleteReservation_success() {
        Long userId = 1L;
        Long reservationId = 10L;

        User user = User.builder().id(userId).build();
        Reservation reservation = Reservation.builder().id(reservationId).user(user).build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        var response = reservationService.deleteReservation(userId, reservationId);

        verify(reservationRepository).delete(reservation);
    }

    @Test
    @DisplayName("예약 취소 성공")
    void cancelReservation_success() {
        Long userId = 1L;
        Long reservationId = 11L;

        User user = User.builder().id(userId).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(user)
                .status(ReservationStatus.PENDING)
                .reservationTime(LocalDateTime.now().plusMinutes(5))
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        var response = reservationService.cancelReservation(userId, reservationId);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("예약 확인 성공")
    void confirmReservation_success() {
        Long ownerId = 1L;
        Long reservationId = 101L;

        Store store = Store.builder().id(200L).owner(User.builder().id(ownerId).build()).build();
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .store(store)
                .status(ReservationStatus.PENDING)
                .build();

        ConfirmReservation.Request request = new ConfirmReservation.Request();
        request.setReservationId(reservationId);
        request.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        var response = reservationService.confirmReservation(ownerId, request);

        assertThat(response.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("체크인 시간 범위 밖")
    void checkInReservation_invalidTime() {
        Long userId = 1L;
        Long reservationId = 500L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(User.builder().id(userId).build())
                .status(ReservationStatus.CONFIRMED)
                .reservationTime(LocalDateTime.now().plusHours(2)) // 너무 이른 시간
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationException.class,
                () -> reservationService.checkInReservation(userId, reservationId));
    }

    @Test
    @DisplayName("이미 체크인된 예약")
    void checkInReservation_alreadyCheckedIn() {
        Long userId = 1L;
        Long reservationId = 600L;

        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .user(User.builder().id(userId).build())
                .status(ReservationStatus.CHECKED_IN)
                .reservationTime(LocalDateTime.now())
                .build();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThrows(ReservationException.class, () -> reservationService.checkInReservation(userId, reservationId));
    }
}
