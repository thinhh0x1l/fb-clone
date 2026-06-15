package com.fb.common.exception;

/**
 * Exception khi vượt quá giới hạn yêu cầu
 */
public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
