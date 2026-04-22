package com.jusin.repository;

import com.jusin.domain.entity.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {
    Optional<PredictionResult> findByCompanyIdAndPeriod(String companyId, String period);
    Optional<PredictionResult> findTopByCompanyIdOrderByCalculatedAtDesc(String companyId);
    List<PredictionResult> findByCompanyIdOrderByPeriodDesc(String companyId);

    @Query("SELECT p FROM PredictionResult p " +
           "WHERE p.companyId = :companyId " +
           "AND p.calculatedAt >= :startDate " +
           "AND p.calculatedAt < :endDate " +
           "ORDER BY p.calculatedAt DESC")
    List<PredictionResult> findHistoryByCompanyIdAndDateRange(
            @Param("companyId") String companyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
