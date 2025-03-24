package com.reservation.service;

import static com.reservation.type.ErrorCode.CANNOT_CREATE_ADMIN;
import static com.reservation.type.ErrorCode.EMAIL_ALREADY_IN_USE;
import static com.reservation.type.ErrorCode.INVALID_PASSWORD;
import static com.reservation.type.ErrorCode.PASSWORD_UNMATCHED;
import static com.reservation.type.ErrorCode.USERID_ALREADY_IN_USE;
import static com.reservation.type.ErrorCode.USERTYPE_NOT_OWNER;
import static com.reservation.type.ErrorCode.USER_NOT_FOUND;
import static com.reservation.type.UserType.ADMIN;
import static com.reservation.type.UserType.OWNER;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.domain.User;
import com.reservation.dto.UserDto;
import com.reservation.dto.store.UpdateStore;
import com.reservation.dto.user.CreateUser;
import com.reservation.dto.user.DeleteUser;
import com.reservation.dto.user.UpdateUser;
import com.reservation.dto.user.UpdateUserPartnership;
import com.reservation.exception.UserException;
import com.reservation.repository.UserRepository;
import com.reservation.type.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 신규 사용자를 생성하는 메소드
	 * - 아이디, 이메일, 전화번호 중복 여부를 확인하고 회원을 등록합니다.
	 * - 관리자(ADMIN) 계정은 생성할 수 없습니다.
	 * 
	 * @param request 사용자 생성 요청 객체
	 * @return 생성된 사용자 엔티티
	 */
	@Transactional
	public User createUser(CreateUser.Request request) {
		// 중복 및 예외 처리
		if(userRepository.existsByUserId(request.getUserId())) {
			throw new UserException(USERID_ALREADY_IN_USE);
		}
		if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException(EMAIL_ALREADY_IN_USE);
        }
		if(userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
			throw new UserException(ErrorCode.PHONE_NUMBER_ALREADY_IN_USE);
		}
	    if(request.getUserType() == ADMIN) {
	    	throw new UserException(CANNOT_CREATE_ADMIN);
	    }

		// 사용자 생성
		User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .userType(request.getUserType())
                .phoneNumber(request.getPhoneNumber())
                .build();
		
		return userRepository.save(user);
	}

	/**
	 * 사용자를 삭제하는 메소드
	 * - 비밀번호 검증 후 해당 사용자를 DB에서 삭제합니다.
	 *
	 * @param userId 인증된 사용자 ID
	 * @param request 비밀번호 확인 요청
	 * @return 삭제된 사용자 정보
	 */
	@Transactional
	public DeleteUser.Response deleteUser(Long userId, DeleteUser.Request request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));
	    
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }
	    
	    userRepository.delete(user);
	    
	    return DeleteUser.Response.from(UserDto.fromEntity(user));
	}

	/**
	 * 사용자 정보를 수정하는 메소드
	 * - 이메일, 닉네임, 전화번호, 유저 타입 변경 가능
	 * - 비밀번호 확인이 필요하며, 관리자 계정으로의 변경은 불가합니다.
	 *
	 * @param userId 인증된 사용자 ID
	 * @param request 수정 요청 객체
	 * @return 수정된 사용자 엔티티
	 */
	@Transactional
	public User updateUser(Long userId, UpdateUser.Request request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));

	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }
	    if(request.getUserType() == ADMIN) {
	    	throw new UserException(CANNOT_CREATE_ADMIN);
	    }

		user.setEmail(request.getEmail());
		user.setNickname(request.getNickname());
		user.setUserType(request.getUserType());
		user.setPhoneNumber(request.getPhoneNumber());

	    if (!request.getPassword().equals(user.getPassword())) {
	        user.setPassword(passwordEncoder.encode(request.getPassword()));
	    }

		return user;
	}

	/**
	 * 점주의 파트너 등록 상태를 true로 변경하는 메소드
	 * - 사용자 유형이 OWNER일 경우만 허용되며, 비밀번호 검증이 필요합니다.
	 *
	 * @param userId 인증된 사용자 ID
	 * @param request 비밀번호 요청 객체
	 * @return 업데이트된 사용자 엔티티
	 */
	@Transactional
	public User updateIsPartner(Long userId, UpdateUserPartnership.Request request) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));

		if(user.getUserType() != OWNER) {
			throw new UserException(USERTYPE_NOT_OWNER);
		}
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }

	    user.setPartner(true);
	    userRepository.save(user);

		return user;
	}

	/**
	 * 사용자의 이메일과 비밀번호를 이용해 로그인(인증)하는 메소드
	 * - 사용자가 존재하는지, 비밀번호가 일치하는지를 확인합니다.
	 *
	 * @param email 사용자 이메일
	 * @param password 입력된 비밀번호
	 * @return 인증된 사용자 객체
	 */
	public User validateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserException(USER_NOT_FOUND);
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(INVALID_PASSWORD);
        }

        return user;
    }
}
