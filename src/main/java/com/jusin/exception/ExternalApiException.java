package com.jusin.exception;

import org.springframework.http.HttpStatus;

public class ExternalApiException extends JusinException {

    public ExternalApiException(String message) {
        super("EXTERNAL_API_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public ExternalApiException(String message, Throwable cause) {
        super("EXTERNAL_API_ERROR", message, HttpStatus.SERVICE_UNAVAILABLE);
        initCause(cause);
    }
}
