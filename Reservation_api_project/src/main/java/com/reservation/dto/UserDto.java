package com.reservation.dto;

import java.time.LocalDateTime;

import com.reservation.domain.User;
import com.reservation.type.UserType;

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
public class UserDto {
	private Long id;
	private String userId;
	private String nickname;
    private boolean isPartner = false;
    private String email;
    
    private LocalDateTime createdAt;    
    private LocalDateTime updatedAt;
    
    private UserType userType;
    private String phoneNumber;
    
	public static UserDto fromEntity(User user) {
		return UserDto.builder()
				.id(user.getId())
				.userId(user.getUserId())
				.nickname(user.getNickname())
				.isPartner(user.isPartner())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.userType(user.getUserType())
				.email(user.getEmail())
				.phoneNumber(user.getPhoneNumber())
				.build();
	}
}
