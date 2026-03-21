package com.jusin.domain.entity;

import com.jusin.domain.enums.SyncStatus;
import com.jusin.domain.enums.SyncType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_sync_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DataSyncLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", length = 8)
    private String companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_type", length = 20)
    private SyncType syncType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SyncStatus status;

    @Column(length = 255)
    private String message;

    @Column(name = "synced_records")
    private Integer syncedRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;

    @Builder
    public DataSyncLog(String companyId, SyncType syncType, SyncStatus status,
                       String message, Integer syncedRecords, Integer failedRecords,
                       LocalDateTime nextSyncAt) {
        this.companyId = companyId;
        this.syncType = syncType;
        this.status = status;
        this.message = message;
        this.syncedRecords = syncedRecords;
        this.failedRecords = failedRecords;
        this.syncedAt = LocalDateTime.now();
        this.nextSyncAt = nextSyncAt;
    }
}
