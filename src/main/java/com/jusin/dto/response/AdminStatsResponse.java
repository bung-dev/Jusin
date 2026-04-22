package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStatsResponse {
    private long totalCompanies;
    private long totalFinancialStatements;
    private long totalPredictionResults;
    private long todaySyncCount;
    private int dartApiDailyLimit;
    private int dartApiUsedToday;
}
