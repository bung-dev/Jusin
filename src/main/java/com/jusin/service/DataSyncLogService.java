package com.jusin.service;

import com.jusin.domain.entity.DataSyncLog;
import com.jusin.domain.enums.SyncStatus;
import com.jusin.domain.enums.SyncType;
import com.jusin.repository.DataSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DataSyncLogService {

    private final DataSyncLogRepository syncLogRepository;

    public void logSuccess(String companyId, SyncType syncType, int syncedRecords) {
        DataSyncLog log = DataSyncLog.builder()
                .companyId(companyId)
                .syncType(syncType)
                .status(SyncStatus.SUCCESS)
                .syncedRecords(syncedRecords)
                .failedRecords(0)
                .nextSyncAt(LocalDateTime.now().plusDays(1))
                .build();
        syncLogRepository.save(log);
    }

    public void logFailure(String companyId, SyncType syncType, String errorMessage) {
        DataSyncLog log = DataSyncLog.builder()
                .companyId(companyId)
                .syncType(syncType)
                .status(SyncStatus.FAILED)
                .message(errorMessage)
                .syncedRecords(0)
                .failedRecords(1)
                .nextSyncAt(LocalDateTime.now().plusHours(6))
                .build();
        syncLogRepository.save(log);
    }
}
