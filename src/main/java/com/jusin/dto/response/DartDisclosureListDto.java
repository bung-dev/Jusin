package com.jusin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class DartDisclosureListDto {
    private String status;
    private String message;

    @JsonProperty("total_count")
    private Integer totalCount;

    private List<DisclosureItem> list;

    @Getter
    @NoArgsConstructor
    public static class DisclosureItem {
        @JsonProperty("corp_code")
        private String corpCode;

        @JsonProperty("corp_name")
        private String corpName;

        @JsonProperty("stock_code")
        private String stockCode;

        @JsonProperty("rcept_no")
        private String rceptNo;

        @JsonProperty("report_nm")
        private String reportNm;

        @JsonProperty("rcept_dt")
        private String rceptDt;

        @JsonProperty("flr_nm")
        private String flrNm;

        private String rmk;
    }
}
