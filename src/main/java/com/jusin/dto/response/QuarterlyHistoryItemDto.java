package com.jusin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jusin.domain.entity.PredictionResult;
import com.jusin.domain.enums.Signal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuarterlyHistoryItemDto {

    private String period;
    private String signal;
    private String signalLevel;
    private int score;
    private String emoji;

    @JsonProperty("isChanged")
    private Boolean isChanged;

    private String previousSignal;

    public static QuarterlyHistoryItemDto from(PredictionResult result,
                                                Boolean isChanged,
                                                Signal previousSignal) {
        return QuarterlyHistoryItemDto.builder()
                .period(result.getPeriod())
                .signal(result.getSignal().name())
                .signalLevel(result.getSignalLevel().name())
                .score(result.getTotalScore())
                .emoji(result.getEmoji())
                .isChanged(isChanged)
                .previousSignal(previousSignal != null ? previousSignal.name() : null)
                .build();
    }
}
