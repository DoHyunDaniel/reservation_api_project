package com.reservation.service;

import static com.reservation.type.ErrorCode.USERID_ALREADY_IN_USE;
import static com.reservation.type.UserType.OWNER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reservation.domain.User;
import com.reservation.dto.user.CreateUser;
import com.reservation.exception.UserException;
import com.reservation.repository.UserRepository;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    
    // 1. 사용자 생성 테스트
    @Test
    void createUser_success() {
        // given
        CreateUser.Request request = new CreateUser.Request(
                1L,
                "user123",
                "닉네임",
                "password123",
                "test@example.com",
                OWNER,
                "010-1234-5678"
        );

        when(userRepository.existsByUserId(request.getUserId())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(request.getPhoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        User userToSave = User.builder()
                .userId(request.getUserId())
                .nickname(request.getNickname())
                .password("encodedPassword")
                .email(request.getEmail())
                .userType(request.getUserType())
                .phoneNumber(request.getPhoneNumber())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        // when
        User savedUser = userService.createUser(request);

        // then
        assertNotNull(savedUser);
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("010-1234-5678", savedUser.getPhoneNumber());
    }

    @Test
    void createUser_duplicateUserId() {
        // given
        CreateUser.Request request = new CreateUser.Request(
                1L,
                "user123",
                "닉네임",
                "password123",
                "test@example.com",
                OWNER,
                "010-1234-5678"
        );

        when(userRepository.existsByUserId(request.getUserId())).thenReturn(true);

        // when & then
        UserException ex = assertThrows(UserException.class, () -> userService.createUser(request));
        assertEquals(USERID_ALREADY_IN_USE, ex.getErrorCode());
    }
}
