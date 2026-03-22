package com.jusin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DartCompanyDto {
    private String status;
    private String message;

    @JsonProperty("corp_code")
    private String corpCode;

    @JsonProperty("corp_name")
    private String corpName;

    @JsonProperty("stock_code")
    private String stockCode;

    @JsonProperty("corp_cls")
    private String corpCls;

    @JsonProperty("ceo_nm")
    private String ceoNm;

    private String adres;

    @JsonProperty("hm_url")
    private String hmUrl;

    @JsonProperty("phn_no")
    private String phnNo;

    @JsonProperty("induty_code")
    private String indutyCode;

    @JsonProperty("est_dt")
    private String estDt;

    @JsonProperty("acc_mt")
    private String acctMt;
}
