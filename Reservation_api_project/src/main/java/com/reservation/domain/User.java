package com.reservation.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.reservation.type.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EnableJpaAuditing
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    @Column(name = "user_name", unique = true, nullable = false, length = 10) 
    private String userId;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(nullable = false, length = 255) 
    private String password;

    @Column(name = "is_partner", nullable = false)
    private boolean isPartner = false;

    @CreatedDate  
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = true, updatable = true)
    private LocalDateTime updatedAt;
    
    @Enumerated(EnumType.STRING) 
    @Column(name = "user_type", nullable = false)
    private UserType userType; // 회원 유형 (ADMIN, OWNER, CUSTOMER)

    @Column(unique = true, nullable = false, length = 50)  // 이메일 필드 추가
    private String email;
}
