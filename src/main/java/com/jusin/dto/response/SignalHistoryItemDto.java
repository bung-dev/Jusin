package com.jusin.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jusin.domain.entity.PredictionResult;
import com.jusin.domain.enums.Signal;
import com.jusin.domain.enums.SignalLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignalHistoryItemDto {
    private String date;
    private Signal signal;
    private SignalLevel signalLevel;
    private Integer score;
    private String emoji;
    @JsonProperty("isChanged")
    private Boolean isChanged;
    private Signal changeFrom;

    public static SignalHistoryItemDto from(PredictionResult result, Boolean isChanged, Signal previousSignal) {
        return SignalHistoryItemDto.builder()
                .date(result.getCalculatedAt().toLocalDate().toString())
                .signal(result.getSignal())
                .signalLevel(result.getSignalLevel())
                .score(result.getTotalScore())
                .emoji(result.getEmoji())
                .isChanged(isChanged)
                .changeFrom(isChanged ? previousSignal : null)
                .build();
    }
}
