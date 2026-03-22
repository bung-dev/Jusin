package com.jusin.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ParsedFinancialData {

    private String period;

    // 손익계산서
    private BigDecimal revenue;
    private BigDecimal operatingIncome;
    private BigDecimal netIncome;
    private Long shareCount;

    // 재무상태표
    private BigDecimal totalAssets;
    private BigDecimal currentAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal currentLiabilities;
    private BigDecimal equity;

    // 현금흐름표
    private BigDecimal operatingCashFlow;

    public boolean isIncomeStatementComplete() {
        return revenue != null && operatingIncome != null && netIncome != null;
    }

    public boolean isBalanceSheetComplete() {
        return totalAssets != null && totalLiabilities != null && equity != null
                && currentAssets != null && currentLiabilities != null;
    }
}
