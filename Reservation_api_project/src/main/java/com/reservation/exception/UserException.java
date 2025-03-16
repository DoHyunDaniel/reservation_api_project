package com.reservation.exception;

import com.reservation.type.ErrorCode;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final ErrorCode errorCode;

    public UserException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }
}
