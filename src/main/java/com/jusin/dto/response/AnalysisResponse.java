package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AnalysisResponse {

    private String companyName;
    private String stockCode;
    private String analysisDate;
    private String lastDataDate;
    private PredictionDto prediction;
    private Map<String, IndicatorDto> indicators;
    private FinancialDataDto financialData;
    private List<String> notes;

    @Getter
    @Builder
    public static class PredictionDto {
        private String signal;
        private String signalLevel;
        private int score;
        private String scoreLevel;
        private String emoji;
    }

    @Getter
    @Builder
    public static class FinancialDataDto {
        private String period;
        private String revenue;
        private String operatingIncome;
        private String netIncome;
        private String totalAssets;
        private String equity;
    }
}
