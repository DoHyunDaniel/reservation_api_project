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
import com.reservation.dto.CreateUser;
import com.reservation.dto.DeleteUser;
import com.reservation.dto.UpdateUser;
import com.reservation.dto.UpdateUserPartnership;
import com.reservation.dto.UserDto;
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
	
	@Transactional
	public User createUser(CreateUser.Request request) {
		
		// 이미 사용중인 아이디
		if(userRepository.existsByUserId(request.getUserId())) {
			throw new UserException(USERID_ALREADY_IN_USE);
		}
		
		// 이미 사용중인 이메일
		if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserException(EMAIL_ALREADY_IN_USE);
        }
		
		// 이미 사용중인 전화번호
		if(userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
			throw new UserException(ErrorCode.PHONE_NUMBER_ALREADY_IN_USE);
		}
		
		// 관리자 계정으로는 생성 불가
	    if(request.getUserType()==ADMIN) {
	    	throw new UserException(CANNOT_CREATE_ADMIN);
	    }
		
		User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .nickname(request.getNickname())
                .email(request.getEmail())
                .userType(request.getUserType())
                .phoneNumber(request.getPhoneNumber())
                .build();
		
		return userRepository.save(user);
	}
	
	@Transactional
	public DeleteUser.Response deleteUser(Long userId, DeleteUser.Request request) {
		User user = userRepository.findById(userId).orElseThrow(()-> new UserException(USER_NOT_FOUND));
	    
		// 비밀번호 확인
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }
	    
	    userRepository.delete(user);
	    
	    return DeleteUser.Response.from(UserDto.fromEntity(user));
	
	}
	
	/*
	 * 비밀번호, 닉네임, 유저타입(점주, 일반)만 변경
	 */
	@Transactional
	public User updateUser(Long userId, UpdateUser.Request request) {
		User user = userRepository.findById(userId).orElseThrow(()->new UserException(USER_NOT_FOUND));
		
		// 비밀번호 확인
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }
		// 관리자 계정으로는 생성 불가
	    if(request.getUserType()==ADMIN) {
	    	throw new UserException(CANNOT_CREATE_ADMIN);
	    }
	    
		user.setEmail(request.getEmail());
		user.setNickname(request.getNickname());
		user.setUserType(request.getUserType());
		user.setPhoneNumber(request.getPhoneNumber());
		
	    // 비밀번호가 변경되었을 경우만 암호화하여 업데이트
	    if (!request.getPassword().equals(user.getPassword())) {
	        user.setPassword(passwordEncoder.encode(request.getPassword()));
	    }
		
		return user;
	}
	
	/*
	 * 비밀번호 확인 후 파트너 여부 변경
	 */
	@Transactional
	public User updateIsPartner(Long userId, UpdateUserPartnership.Request request) {
		
		User user = userRepository.findById(userId).orElseThrow(()->new UserException(USER_NOT_FOUND));
		
		// 사용자 유형이 OWNER가 아닐 경우 예외 발생
		if(user.getUserType()!=OWNER) {
			throw new UserException(USERTYPE_NOT_OWNER);
		}
		
		// 비밀번호 확인
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	        throw new UserException(PASSWORD_UNMATCHED);
	    }
	    
	    user.setPartner(true);
	    userRepository.save(user);
	    
		return user;
	}
	
    // 사용자 인증(이메일, 비밀번호)
    public User validateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserException(USER_NOT_FOUND);
        }

        User user = userOptional.get();

        // 비밀번호 검증 (평문 vs 암호화된 비밀번호 비교)
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(INVALID_PASSWORD);
        }

        return user;
    }
}
