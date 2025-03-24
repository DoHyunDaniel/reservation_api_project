package com.reservation.service;

import static com.reservation.type.ErrorCode.CANNOT_CREATE_ADMIN;
import static com.reservation.type.ErrorCode.EMAIL_ALREADY_IN_USE;
import static com.reservation.type.ErrorCode.PHONE_NUMBER_ALREADY_IN_USE;
import static com.reservation.type.ErrorCode.USERID_ALREADY_IN_USE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.reservation.domain.User;
import com.reservation.dto.user.CreateUser;
import com.reservation.dto.user.DeleteUser;
import com.reservation.dto.user.UpdateUser;
import com.reservation.exception.UserException;
import com.reservation.repository.UserRepository;
import com.reservation.type.UserType;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("정상적으로 사용자 생성")
	void createUser_success() {
		// given
		CreateUser.Request request = new CreateUser.Request();
		request.setUserId("testUser");
		request.setEmail("test@example.com");
		request.setPassword("password123");
		request.setNickname("tester");
		request.setPhoneNumber("010-1234-5678");
		request.setUserType(UserType.VISITER);
		
		when(userRepository.existsByUserId("testUser")).thenReturn(false);
		when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(userRepository.existsByPhoneNumber("010-1234-5678")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// when
		User savedUser = userService.createUser(request);

		// then
		assertThat(savedUser.getUserId()).isEqualTo("testUser");
		assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
	}

	@Test
	@DisplayName("아이디 중복으로 사용자 생성 실패")
	void createUser_userIdDuplicate() {
		CreateUser.Request request = new CreateUser.Request();
		request.setUserId("testUser");

		when(userRepository.existsByUserId("testUser")).thenReturn(true);

		UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
		assertThat(exception.getErrorCode()).isEqualTo(USERID_ALREADY_IN_USE);
	}

	@Test
	@DisplayName("이메일 중복으로 사용자 생성 실패")
	void createUser_emailDuplicate() {
		CreateUser.Request request = new CreateUser.Request();
		request.setUserId("newUser");
		request.setEmail("test@example.com");

		when(userRepository.existsByUserId("newUser")).thenReturn(false);
		when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

		UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
		assertThat(exception.getErrorCode()).isEqualTo(EMAIL_ALREADY_IN_USE);
	}

	@Test
	@DisplayName("전화번호 중복으로 사용자 생성 실패")
	void createUser_phoneDuplicate() {
		CreateUser.Request request = new CreateUser.Request();
		request.setUserId("newUser");
		request.setEmail("test@example.com");
		request.setPhoneNumber("010-1111-1111");

		when(userRepository.existsByUserId("newUser")).thenReturn(false);
		when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
		when(userRepository.existsByPhoneNumber("010-1111-1111")).thenReturn(true);

		UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
		assertThat(exception.getErrorCode()).isEqualTo(PHONE_NUMBER_ALREADY_IN_USE);
	}

	@Test
	@DisplayName("관리자 계정 생성 시도 실패")
	void createUser_adminNotAllowed() {
		CreateUser.Request request = new CreateUser.Request();
		request.setUserId("adminUser");
		request.setEmail("admin@example.com");
		request.setPhoneNumber("010-1111-1111");
		request.setUserType(UserType.ADMIN);

		when(userRepository.existsByUserId("adminUser")).thenReturn(false);
		when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
		when(userRepository.existsByPhoneNumber("010-1111-1111")).thenReturn(false);

		UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
		assertThat(exception.getErrorCode()).isEqualTo(CANNOT_CREATE_ADMIN);
	}

	// deleteUser() 단위 테스트
	@Test
	@DisplayName("정상적으로 사용자 삭제")
	void deleteUser_success() {
		// given
		Long userId = 1L;
		String rawPassword = "password123";
		User user = User.builder().id(userId).password("encodedPassword").email("test@example.com").userId("testUser")
				.build();

		DeleteUser.Request request = new DeleteUser.Request();
		request.setPassword(rawPassword);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(rawPassword, user.getPassword())).thenReturn(true);

		// when
		DeleteUser.Response response = userService.deleteUser(userId, request);

		// then
		verify(userRepository, times(1)).delete(user);
	}

	@Test
	@DisplayName("삭제 시 비밀번호 불일치")
	void deleteUser_passwordMismatch() {
		Long userId = 1L;
		User user = User.builder().id(userId).password("encodedPassword").build();

		DeleteUser.Request request = new DeleteUser.Request();
		request.setPassword("wrongPassword");

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

		assertThrows(UserException.class, () -> userService.deleteUser(userId, request));
	}

	// updateUser() 단위 테스트
	@Test
	@DisplayName("정상적으로 사용자 업데이트")
	void updateUser_success() {
		Long userId = 1L;
		User user = User.builder().id(userId).userType(UserType.VISITER).password("encodedPassword").build();

		UpdateUser.Request request = new UpdateUser.Request();
		request.setPassword("password123");
		request.setEmail("new@email.com");
		request.setNickname("newNick");
		request.setPhoneNumber("010-2222-2222");
		request.setUserType(UserType.OWNER);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
		when(passwordEncoder.encode("password123")).thenReturn("encodedPassword"); // password same, no update

		User updatedUser = userService.updateUser(userId, request);

		assertThat(updatedUser.getNickname()).isEqualTo("newNick");
		assertThat(updatedUser.getUserType()).isEqualTo(UserType.OWNER);
	}

	@Test
	@DisplayName("업데이트 시 관리자 권한으로 변경 시도")
	void updateUser_adminNotAllowed() {
		Long userId = 1L;
		User user = User.builder().id(userId).userType(UserType.VISITER).password("encodedPassword").build();

		UpdateUser.Request request = new UpdateUser.Request();
		request.setPassword("password123");
		request.setUserType(UserType.ADMIN);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

		assertThrows(UserException.class, () -> userService.updateUser(userId, request));
	}
	
	// validateUser() 단위 테스트
	@Test
	@DisplayName("정상적인 사용자 인증")
	void validateUser_success() {
	    String email = "test@example.com";
	    String rawPassword = "password123";

	    User user = User.builder()
	            .email(email)
	            .password("encodedPassword")
	            .build();

	    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
	    when(passwordEncoder.matches(rawPassword, "encodedPassword")).thenReturn(true);

	    User result = userService.validateUser(email, rawPassword);

	    assertThat(result.getEmail()).isEqualTo(email);
	}

	@Test
	@DisplayName("비밀번호 불일치로 인증 실패")
	void validateUser_invalidPassword() {
	    String email = "test@example.com";

	    User user = User.builder()
	            .email(email)
	            .password("encodedPassword")
	            .build();

	    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
	    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

	    assertThrows(UserException.class, () -> userService.validateUser(email, "wrongPassword"));
	}


}
