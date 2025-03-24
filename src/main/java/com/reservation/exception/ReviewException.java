package com.reservation.exception;

import com.reservation.type.ErrorCode;

import lombok.Getter;

@Getter
public class ReviewException extends RuntimeException{
    private final ErrorCode errorCode;

    public ReviewException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
