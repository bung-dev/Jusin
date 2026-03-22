package com.jusin.exception;

import org.springframework.http.HttpStatus;

public class CompanyNotFoundException extends JusinException {

    public CompanyNotFoundException(String stockCode) {
        super("COMPANY_NOT_FOUND",
              "종목코드 " + stockCode + "에 해당하는 기업을 찾을 수 없습니다.",
              HttpStatus.NOT_FOUND);
    }
}
