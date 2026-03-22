package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class IndicatorDto {

    private BigDecimal value;
    private Integer score;
    private Integer maxScore;
    private String evaluation;

    public static IndicatorDto of(BigDecimal value, int score, int maxScore, String evaluation) {
        return IndicatorDto.builder()
                .value(value)
                .score(score)
                .maxScore(maxScore)
                .evaluation(evaluation)
                .build();
    }
}
