package com.jusin.dto.response;

import com.jusin.domain.entity.Company;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompanySearchResponse {

    private String companyId;
    private String companyName;
    private String stockCode;
    private String sector;
    private String representative;

    public static CompanySearchResponse from(Company company) {
        return CompanySearchResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .stockCode(company.getStockCode())
                .sector(company.getSector())
                .representative(company.getRepresentative())
                .build();
    }
}
