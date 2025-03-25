package com.reservation.service;

import static com.reservation.type.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import com.reservation.domain.Store;
import com.reservation.domain.User;
import com.reservation.dto.StoreDto;
import com.reservation.dto.store.DeleteStore;
import com.reservation.dto.store.RegisterStore;
import com.reservation.dto.store.UpdateStore;
import com.reservation.exception.UserException;
import com.reservation.repository.StoreRepository;
import com.reservation.repository.UserRepository;
import com.reservation.type.UserType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

public class StoreServiceTest {

    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("정상적으로 가게 등록")
    void registerStore_success() {
        // given
        Long userId = 1L;
        User owner = User.builder()
                .id(userId)
                .userType(UserType.OWNER)
                .isPartner(true)
                .build();

        StoreDto storeDto = StoreDto.builder()
                .storeName("Test Store")
                .lat(37.123)
                .lng(127.456)
                .detail("Test detail")
                .build();

        Store savedStore = Store.builder()
                .id(100L)
                .storeName("Test Store")
                .owner(owner)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        // when
        RegisterStore.Response response = storeService.registerStore(userId, storeDto);

        // then
        assertThat(response.getStoreName()).isEqualTo("Test Store");
        verify(storeRepository, times(1)).save(any(Store.class));
    }

    @Test
    @DisplayName("가게 삭제 실패 - 비밀번호 불일치")
    void deleteStore_passwordMismatch() {
        Long userId = 1L;
        User owner = User.builder().id(userId).password("encodedPassword").build();
        Store store = Store.builder().id(10L).owner(owner).build();

        DeleteStore.Request request = new DeleteStore.Request();
        request.setId(10L);
        request.setPassword("wrongPassword");

        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UserException.class, () -> storeService.deleteStore(userId, request));
    }

    @Test
    @DisplayName("가게 삭제 성공")
    void deleteStore_success() {
        Long userId = 1L;
        User owner = User.builder().id(userId).password("encodedPassword").build();
        Store store = Store.builder().id(10L).owner(owner).storeName("Test Store").build();

        DeleteStore.Request request = new DeleteStore.Request();
        request.setId(10L);
        request.setPassword("correctPassword");

        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

        DeleteStore.Response response = storeService.deleteStore(userId, request);

        verify(storeRepository, times(1)).delete(store);
    }

    @Test
    @DisplayName("가게 수정 실패 - 관리자 권한으로 변경 시도")
    void updateStore_adminRoleNotAllowed() {
        Long userId = 1L;
        User owner = User.builder().id(userId).password("encodedPassword").build();
        Store store = Store.builder().id(20L).owner(owner).build();

        UpdateStore.Request request = new UpdateStore.Request();
        request.setId(20L);
        request.setPassword("correctPassword");
        request.setUserType(UserType.ADMIN);

        when(storeRepository.findById(20L)).thenReturn(Optional.of(store));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

        assertThrows(UserException.class, () -> storeService.updateStore(userId, request));
    }

    @Test
    @DisplayName("가게 수정 성공")
    void updateStore_success() {
        Long userId = 1L;
        User owner = User.builder().id(userId).password("encodedPassword").build();
        Store store = Store.builder().id(20L).owner(owner).build();

        UpdateStore.Request request = new UpdateStore.Request();
        request.setId(20L);
        request.setPassword("correctPassword");
        request.setStoreName("Updated Store");
        request.setLat(35.0);
        request.setLng(127.0);
        request.setDetail("New detail");
        request.setUserType(UserType.OWNER);

        when(storeRepository.findById(20L)).thenReturn(Optional.of(store));
        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

        Store updatedStore = storeService.updateStore(userId, request);

        assertThat(updatedStore.getStoreName()).isEqualTo("Updated Store");
        assertThat(updatedStore.getLat()).isEqualTo(35.0);
    }
}
