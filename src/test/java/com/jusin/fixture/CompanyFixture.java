package com.jusin.fixture;

import com.jusin.domain.entity.Company;

public class CompanyFixture {

    public static Company samsungElectronics() {
        return Company.builder()
                .companyId("00126380")
                .companyName("삼성전자")
                .stockCode("005930")
                .sector("전자")
                .representative("한종희")
                .address("경기도 수원시 영통구 삼성로 1")
                .build();
    }
}
