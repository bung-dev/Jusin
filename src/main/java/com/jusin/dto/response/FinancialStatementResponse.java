package com.jusin.dto.response;

import com.jusin.domain.entity.FinancialStatement;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FinancialStatementResponse {

    private String stockCode;
    private String period;
    private IncomeStatementDto incomeStatement;
    private BalanceSheetDto balanceSheet;
    private CashFlowDto cashFlowStatement;

    @Getter
    @Builder
    public static class IncomeStatementDto {
        private String revenue;
        private String operatingIncome;
        private String netIncome;
        private Long shareCount;
    }

    @Getter
    @Builder
    public static class BalanceSheetDto {
        private String currentAssets;
        private String totalAssets;
        private String currentLiabilities;
        private String totalLiabilities;
        private String equity;
    }

    @Getter
    @Builder
    public static class CashFlowDto {
        private String operatingActivities;
    }

    public static FinancialStatementResponse from(FinancialStatement fs) {
        return FinancialStatementResponse.builder()
                .stockCode(fs.getCompanyId())
                .period(fs.getPeriod())
                .incomeStatement(IncomeStatementDto.builder()
                        .revenue(formatAmount(fs.getRevenue()))
                        .operatingIncome(formatAmount(fs.getOperatingIncome()))
                        .netIncome(formatAmount(fs.getNetIncome()))
                        .shareCount(fs.getShareCount())
                        .build())
                .balanceSheet(BalanceSheetDto.builder()
                        .currentAssets(formatAmount(fs.getCurrentAssets()))
                        .totalAssets(formatAmount(fs.getTotalAssets()))
                        .currentLiabilities(formatAmount(fs.getCurrentLiabilities()))
                        .totalLiabilities(formatAmount(fs.getTotalLiabilities()))
                        .equity(formatAmount(fs.getEquity()))
                        .build())
                .cashFlowStatement(CashFlowDto.builder()
                        .operatingActivities(formatAmount(fs.getOperatingCashFlow()))
                        .build())
                .build();
    }

    private static String formatAmount(BigDecimal amount) {
        return amount != null ? amount.toPlainString() : null;
    }
}
