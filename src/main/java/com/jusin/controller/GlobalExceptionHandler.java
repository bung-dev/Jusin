package com.jusin.controller;

import com.jusin.dto.response.ErrorResponse;
import com.jusin.exception.JusinException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(RuntimeException e) {
        log.warn("인증 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."));
    }

    @ExceptionHandler(JusinException.class)
    public ResponseEntity<ErrorResponse> handleJusinException(JusinException e,
                                                               HttpServletRequest req) {
        log.warn("[{}] {} - {}", e.getErrorCode(), req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(ErrorResponse.of(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[VALIDATION_ERROR] {}", msg);
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("MISSING_PARAMETER",
                        "필수 파라미터가 누락되었습니다: " + e.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST_BODY", "요청 본문을 파싱할 수 없습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.of("METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다: " + e.getMethod()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("요청 파라미터 오류: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, HttpServletRequest req) {
        log.error("[INTERNAL_SERVER_ERROR] {} - {}", req.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
