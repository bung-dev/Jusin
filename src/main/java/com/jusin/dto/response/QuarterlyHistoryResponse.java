package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class QuarterlyHistoryResponse {

    private String stockCode;
    private String companyName;
    private int quarters;
    private int historyCount;
    private List<QuarterlyHistoryItemDto> history;

    public static QuarterlyHistoryResponse of(String stockCode, String companyName,
                                               int quarters,
                                               List<QuarterlyHistoryItemDto> history) {
        return QuarterlyHistoryResponse.builder()
                .stockCode(stockCode)
                .companyName(companyName)
                .quarters(quarters)
                .historyCount(history.size())
                .history(history)
                .build();
    }
}
