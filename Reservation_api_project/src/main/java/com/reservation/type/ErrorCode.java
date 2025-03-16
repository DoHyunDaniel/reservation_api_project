package com.reservation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND("해당 사용자가 존재하지 않습니다."),
	PASSWORD_UNMATCHED("비밀번호가 일치하지 않습니다."),
	CANNOT_CREATE_ADMIN("관리자 계정은 생성할 수 없습니다."),
	USERID_ALREADY_IN_USE("이미 존재하는 아이디입니다."),
	EMAIL_ALREADY_IN_USE("이미 사용중인 이메일입니다."),
	USERTYPE_NOT_OWNER("파트너십 가입은 점주 계정만 할 수 있습니다."),
	INVALID_PASSWORD("패스워드가 틀립니다."),
	OWNER_NOT_FOUND("점주 정보를 찾을 수 없습니다."),
	INVALID_ROLE("권한이 없습니다."), PHONE_NUMBER_ALREADY_IN_USE("이미 사용중인 전화번호입니다.");
	private final String description;
}
