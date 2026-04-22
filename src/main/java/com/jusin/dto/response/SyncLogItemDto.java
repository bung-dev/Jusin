package com.jusin.dto.response;

import com.jusin.domain.entity.DataSyncLog;
import com.jusin.domain.enums.SyncStatus;
import com.jusin.domain.enums.SyncType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SyncLogItemDto {
    private Long id;
    private String companyId;
    private SyncType syncType;
    private SyncStatus status;
    private String message;
    private Integer syncedRecords;
    private Integer failedRecords;
    private LocalDateTime syncedAt;
    private LocalDateTime nextSyncAt;

    public static SyncLogItemDto from(DataSyncLog log) {
        return SyncLogItemDto.builder()
                .id(log.getId())
                .companyId(log.getCompanyId())
                .syncType(log.getSyncType())
                .status(log.getStatus())
                .message(log.getMessage())
                .syncedRecords(log.getSyncedRecords())
                .failedRecords(log.getFailedRecords())
                .syncedAt(log.getSyncedAt())
                .nextSyncAt(log.getNextSyncAt())
                .build();
    }
}
