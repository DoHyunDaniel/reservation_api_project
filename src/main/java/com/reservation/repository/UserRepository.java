package com.reservation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 사용자 ID가 이미 존재하는지 여부를 확인하는 메소드
     * - 회원가입 시 사용자 ID 중복 검사를 위해 사용
     *
     * @param userId 사용자 ID
     * @return 중복 여부 (true: 존재함, false: 사용 가능)
     */
    boolean existsByUserId(String userId);

    /**
     * 주어진 이메일이 이미 등록되어 있는지 여부를 확인하는 메소드
     * - 회원가입 시 이메일 중복 체크에 사용
     *
     * @param email 사용자 이메일
     * @return 중복 여부 (true: 존재함)
     */
    boolean existsByEmail(String email);

    /**
     * 사용자 ID로 사용자 정보를 조회하는 메소드
     * - 주로 로그인, 인증 등에서 사용
     *
     * @param userId 사용자 ID
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByUserId(String userId);

    /**
     * 이메일로 사용자 정보를 조회하는 메소드
     * - 이메일 기반 로그인이나 인증 시 사용
     *
     * @param email 사용자 이메일
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 전화번호가 이미 등록되어 있는지 확인하는 메소드
     * - 회원가입 시 중복 전화번호 체크에 사용
     *
     * @param phoneNumber 사용자 전화번호
     * @return 중복 여부 (true: 이미 존재함)
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
