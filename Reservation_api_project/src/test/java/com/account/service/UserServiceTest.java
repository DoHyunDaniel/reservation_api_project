package com.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reservation.domain.User;
import com.reservation.exception.UserException;
import com.reservation.repository.UserRepository;
import com.reservation.service.UserService;
import com.reservation.type.ErrorCode;

@ExtendWith(MockitoExtension.class) // Mockito 확장 적용
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // 성공적인 로그인 테스트
    @Test
    void validateUser_Success() {
        // given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")  // 암호화된 비밀번호
                .build();

        given(userRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches("password123", "encodedPassword"))
                .willReturn(true); // 비밀번호 일치

        // When
        User user = userService.validateUser("test@example.com", "password123");

        // Then
        assertNotNull(user);
        assertEquals("test@example.com", user.getEmail());
    }

    // 이메일이 존재하지 않는 경우 예외 발생
    @Test
    void validateUser_UserNotFound() {
        // Given
        given(userRepository.findByEmail(anyString()))
                .willReturn(Optional.empty());

        // When & Then
        UserException exception = assertThrows(UserException.class, () ->
                userService.validateUser("wrong@example.com", "password123"));

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    // 비밀번호가 틀린 경우 예외 발생
    @Test
    void validateUser_InvalidPassword() {
        // Given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")  // 암호화된 비밀번호
                .build();

        given(userRepository.findByEmail("test@example.com"))
                .willReturn(Optional.of(mockUser));

        given(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .willReturn(false); // 비밀번호 불일치

        // When & Then
        UserException exception = assertThrows(UserException.class, () ->
                userService.validateUser("test@example.com", "wrongPassword"));

        assertEquals(ErrorCode.INVALID_PASSWORD, exception.getErrorCode());
    }
}
