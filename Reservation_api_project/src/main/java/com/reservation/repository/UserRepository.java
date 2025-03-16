package com.reservation.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUserId(String userId);

	boolean existsByEmail(String email);

	Optional<User> findByUserId(String userId);

	Optional<User> findByEmail(String email);

	boolean existsByPhoneNumber(String phoneNumber);
	
}
