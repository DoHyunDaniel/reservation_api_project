package com.reservation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUserId(String userId);

	boolean existsByEmail(String email);
	
}
