package com.jusin.fixture;

import com.jusin.dto.response.DartCompanyDto;
import org.springframework.test.util.ReflectionTestUtils;

public class DartApiMockFixture {

    public static DartCompanyDto samsungCompanyDto() {
        DartCompanyDto dto = new DartCompanyDto();
        ReflectionTestUtils.setField(dto, "status", "000");
        ReflectionTestUtils.setField(dto, "corpCode", "00126380");
        ReflectionTestUtils.setField(dto, "corpName", "삼성전자");
        ReflectionTestUtils.setField(dto, "stockCode", "005930");
        ReflectionTestUtils.setField(dto, "ceoNm", "한종희");
        ReflectionTestUtils.setField(dto, "adres", "경기도 수원시 영통구");
        return dto;
    }

    public static String samsungFinancialJson() {
        return """
                {
                  "status": "000",
                  "list": [
                    {"account_nm": "매출액",      "thstrm_amount": "77284000000000"},
                    {"account_nm": "영업이익",     "thstrm_amount": "14299000000000"},
                    {"account_nm": "당기순이익",   "thstrm_amount": "12206000000000"},
                    {"account_nm": "자산총계",     "thstrm_amount": "400534000000000"},
                    {"account_nm": "부채총계",     "thstrm_amount": "177512000000000"},
                    {"account_nm": "자본총계",     "thstrm_amount": "223022000000000"},
                    {"account_nm": "유동자산",     "thstrm_amount": "150000000000000"},
                    {"account_nm": "유동부채",     "thstrm_amount": "83000000000000"}
                  ]
                }
                """;
    }
}
