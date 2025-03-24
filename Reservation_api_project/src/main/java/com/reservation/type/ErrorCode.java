package com.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	// 사용자 관련
	USER_NOT_FOUND("해당 사용자가 존재하지 않습니다."),
	PASSWORD_UNMATCHED("비밀번호가 일치하지 않습니다."),
	CANNOT_CREATE_ADMIN("관리자 계정은 생성할 수 없습니다."),
	USERID_ALREADY_IN_USE("이미 존재하는 아이디입니다."),
	EMAIL_ALREADY_IN_USE("이미 사용중인 이메일입니다."),
	USERTYPE_NOT_OWNER("파트너십 가입은 점주 계정만 할 수 있습니다."),
	INVALID_PASSWORD("패스워드가 틀립니다."),
	OWNER_NOT_FOUND("점주 정보를 찾을 수 없습니다."),
	INVALID_ROLE("권한이 없습니다."), 
	PHONE_NUMBER_ALREADY_IN_USE("이미 사용중인 전화번호입니다."), 
	
	// 매장 관련
	NOT_PARTNER("파트너 점주님만 매장을 등록할 수 있습니다."), 
	STORE_NOT_FOUND("해당 매장을 찾을 수 없습니다."), 
	
	// 예약 관련
	RESERVATION_NOT_FOUND("해당 예약을 찾을 수 없습니다."), 
	DUPLICATE_RESERVATION("중복된 예약 요청입니다."), 
	UNAUTHORIZED("권한이 없습니다."), 
	INVALID_RESERVATION_STATUS("이미 처리된 예약입니다."), UNAUTHORIZED_REVIEW_ACCESS("권한이 없습니다."), 
	
	// 리뷰 등록 관련
	IMAGE_UPLOAD_FAILED("이미지 업로드에 실패했습니다.");
	private final String description;
}
