package com.jusin.repository;

import com.jusin.domain.entity.DataSyncLog;
import com.jusin.domain.enums.SyncStatus;
import com.jusin.domain.enums.SyncType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DataSyncLogRepository extends JpaRepository<DataSyncLog, Long> {
    List<DataSyncLog> findByCompanyIdOrderBySyncedAtDesc(String companyId);

    Page<DataSyncLog> findAllByOrderBySyncedAtDesc(Pageable pageable);

    Page<DataSyncLog> findByStatusOrderBySyncedAtDesc(SyncStatus status, Pageable pageable);

    Page<DataSyncLog> findBySyncTypeOrderBySyncedAtDesc(SyncType syncType, Pageable pageable);

    Page<DataSyncLog> findByStatusAndSyncTypeOrderBySyncedAtDesc(SyncStatus status, SyncType syncType, Pageable pageable);

    @Query("SELECT COUNT(d) FROM DataSyncLog d WHERE d.syncedAt >= :startOfDay AND d.syncedAt < :endOfDay AND d.status = 'SUCCESS'")
    long countTodaySuccessSync(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
