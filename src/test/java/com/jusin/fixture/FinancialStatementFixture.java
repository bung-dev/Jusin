package com.jusin.fixture;

import com.jusin.domain.entity.FinancialStatement;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FinancialStatementFixture {

    public static FinancialStatement samsung2025Q4() {
        return FinancialStatement.builder()
                .companyId("00126380")
                .period("2025-Q4")
                .statementType("COMBINED")
                .revenue(new BigDecimal("77284000000000"))
                .operatingIncome(new BigDecimal("14299000000000"))
                .netIncome(new BigDecimal("12206000000000"))
                .shareCount(2700000000L)
                .currentAssets(new BigDecimal("150000000000000"))
                .totalAssets(new BigDecimal("400534000000000"))
                .currentLiabilities(new BigDecimal("83000000000000"))
                .totalLiabilities(new BigDecimal("177512000000000"))
                .equity(new BigDecimal("223022000000000"))
                .reportedDate(LocalDate.of(2026, 3, 1))
                .build();
    }

    public static FinancialStatement samsung2024Q4() {
        return FinancialStatement.builder()
                .companyId("00126380")
                .period("2024-Q4")
                .statementType("COMBINED")
                .revenue(new BigDecimal("65000000000000"))
                .operatingIncome(new BigDecimal("10000000000000"))
                .netIncome(new BigDecimal("8000000000000"))
                .shareCount(2700000000L)
                .currentAssets(new BigDecimal("130000000000000"))
                .totalAssets(new BigDecimal("380000000000000"))
                .currentLiabilities(new BigDecimal("80000000000000"))
                .totalLiabilities(new BigDecimal("160000000000000"))
                .equity(new BigDecimal("220000000000000"))
                .build();
    }
}
