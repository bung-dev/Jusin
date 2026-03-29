package com.jusin.repository;

import com.jusin.domain.entity.DataSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataSyncLogRepository extends JpaRepository<DataSyncLog, Long> {
    List<DataSyncLog> findByCompanyIdOrderBySyncedAtDesc(String companyId);
}
