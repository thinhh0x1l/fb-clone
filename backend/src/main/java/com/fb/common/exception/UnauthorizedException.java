package com.fb.common.exception;

/**
 * Exception khi không có quyền truy cập
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
