package com.reservation.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.domain.User;
import com.reservation.dto.CreateUser;
import com.reservation.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Transactional
	public User createUser(CreateUser.Request request) {
		if(userRepository.existsByUserId(request.getUserId())) {
			throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
		
		User user = User.builder()
                .userId(request.getUserId())
                .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 암호화
                .nickname(request.getNickname())
                .email(request.getEmail())
                .userType(request.getUserType())
                .build();
		
		return userRepository.save(user);
	}
	
	
}
