package com.jusin.service;

import com.jusin.domain.entity.DataSyncLog;
import com.jusin.domain.enums.SyncStatus;
import com.jusin.domain.enums.SyncType;
import com.jusin.dto.response.SyncLogItemDto;
import com.jusin.dto.response.SyncLogPageResponse;
import com.jusin.repository.DataSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public SyncLogPageResponse getLogs(int page, int size, String status, String syncType) {
        Pageable pageable = PageRequest.of(page, size);
        SyncStatus syncStatusFilter = (status != null && !status.isBlank()) ? SyncStatus.valueOf(status) : null;
        SyncType syncTypeFilter = (syncType != null && !syncType.isBlank()) ? SyncType.valueOf(syncType) : null;

        Page<DataSyncLog> logPage;
        if (syncStatusFilter != null && syncTypeFilter != null) {
            logPage = syncLogRepository.findByStatusAndSyncTypeOrderBySyncedAtDesc(syncStatusFilter, syncTypeFilter, pageable);
        } else if (syncStatusFilter != null) {
            logPage = syncLogRepository.findByStatusOrderBySyncedAtDesc(syncStatusFilter, pageable);
        } else if (syncTypeFilter != null) {
            logPage = syncLogRepository.findBySyncTypeOrderBySyncedAtDesc(syncTypeFilter, pageable);
        } else {
            logPage = syncLogRepository.findAllByOrderBySyncedAtDesc(pageable);
        }

        return SyncLogPageResponse.builder()
                .content(logPage.getContent().stream().map(SyncLogItemDto::from).toList())
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .build();
    }
}
