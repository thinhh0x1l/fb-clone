package com.fb.common.exception;

/**
 * Exception khi dữ liệu không hợp lệ
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
