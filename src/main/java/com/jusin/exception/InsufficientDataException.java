package com.jusin.exception;

import org.springframework.http.HttpStatus;

public class InsufficientDataException extends JusinException {

    public InsufficientDataException(String detail) {
        super("INSUFFICIENT_DATA",
              "지표 계산에 필요한 데이터가 부족합니다: " + detail,
              HttpStatus.BAD_REQUEST);
    }
}
