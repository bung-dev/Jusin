package com.jusin.exception;

import org.springframework.http.HttpStatus;

public abstract class JusinException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    protected JusinException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() { return errorCode; }
    public HttpStatus getStatus() { return status; }
}
