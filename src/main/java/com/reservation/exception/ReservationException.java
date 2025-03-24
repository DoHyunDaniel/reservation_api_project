package com.reservation.exception;

import com.reservation.type.ErrorCode;

import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException{
    private final ErrorCode errorCode;

    public ReservationException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
