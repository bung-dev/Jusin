package com.jusin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jusin.domain.entity.Company;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CompanyDetailResponse {

    private String companyId;
    private String companyName;
    private String stockCode;
    private String sector;
    @JsonProperty("listingDate")
    private LocalDate listDate;
    @JsonProperty("ceo")
    private String representative;
    private String address;
    private String website;
    private String phoneNumber;
    private LocalDateTime lastUpdatedDate;
    private String corpCode;

    public static CompanyDetailResponse from(Company company) {
        return CompanyDetailResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .stockCode(company.getStockCode())
                .sector(company.getSector())
                .listDate(company.getListDate())
                .representative(company.getRepresentative())
                .address(company.getAddress())
                .website(company.getWebsite())
                .phoneNumber(company.getPhoneNumber())
                .lastUpdatedDate(company.getUpdatedAt())
                .corpCode(company.getCompanyId())
                .build();
    }
}
