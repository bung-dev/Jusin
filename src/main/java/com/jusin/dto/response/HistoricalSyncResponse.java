package com.jusin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoricalSyncResponse {

    private int successCount;
    private int skippedCount;
    private int failedCount;
    private int totalCompanies;

    public static HistoricalSyncResponse of(int success, int skipped, int failed) {
        return HistoricalSyncResponse.builder()
                .successCount(success)
                .skippedCount(skipped)
                .failedCount(failed)
                .totalCompanies(0)
                .build();
    }

    public static HistoricalSyncResponse ofAll(int success, int skipped, int failed, int total) {
        return HistoricalSyncResponse.builder()
                .successCount(success)
                .skippedCount(skipped)
                .failedCount(failed)
                .totalCompanies(total)
                .build();
    }
}
