package com.reservation.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.reservation.dto.ErrorResponse;

@RestControllerAdvice // 모든 컨트롤러에서 발생하는 예외를 처리
public class GlobalExceptionHandler {

	// 필수값 누락시 예외처리
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(errors);
	}

	// 필요값 누락시 예외처리
	@ExceptionHandler(org.hibernate.PropertyValueException.class)
	public ResponseEntity<ErrorResponse> handlePropertyValueException(org.hibernate.PropertyValueException ex) {
	    ErrorResponse errorResponse = ErrorResponse.builder()
	        .errorCode("INVALID_PROPERTY")
	        .message("필수 입력 항목이 누락되었습니다: " + ex.getPropertyName())
	        .build();
	    
	    return ResponseEntity.badRequest().body(errorResponse);
	}

	
	// 기타 예외 처리
	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorResponse> handleUserException(UserException ex) {
		ErrorResponse errorResponse = ErrorResponse.builder().errorCode(ex.getErrorCode().name())
				.message(ex.getErrorCode().getDescription()).build();

		return ResponseEntity.badRequest().body(errorResponse);
	}
}
