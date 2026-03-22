package com.jusin.exception;

import org.springframework.http.HttpStatus;

public class DataProcessingException extends JusinException {

    public DataProcessingException(String detail) {
        super("DATA_PROCESSING_ERROR",
              "재무제표 데이터 처리 중 오류가 발생했습니다: " + detail,
              HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public DataProcessingException(String detail, Throwable cause) {
        super("DATA_PROCESSING_ERROR",
              "재무제표 데이터 처리 중 오류가 발생했습니다: " + detail,
              HttpStatus.INTERNAL_SERVER_ERROR);
        initCause(cause);
    }
}
