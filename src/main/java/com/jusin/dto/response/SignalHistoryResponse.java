package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class SignalHistoryResponse {
    private String companyName;
    private String stockCode;
    private String period;
    private Integer historyCount;
    private List<SignalHistoryItemDto> history;

    public static SignalHistoryResponse of(String companyName, String stockCode, String period, List<SignalHistoryItemDto> history) {
        return SignalHistoryResponse.builder()
                .companyName(companyName)
                .stockCode(stockCode)
                .period(period)
                .historyCount(history.size())
                .history(history)
                .build();
    }
}
