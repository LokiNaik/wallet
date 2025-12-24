package com.wallet.exception;

import java.time.LocalDateTime;

public class ApiError {

    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;

    public ApiError(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
