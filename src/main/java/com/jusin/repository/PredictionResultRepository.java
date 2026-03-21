package com.jusin.repository;

import com.jusin.domain.entity.PredictionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {
    Optional<PredictionResult> findByCompanyIdAndPeriod(String companyId, String period);
    Optional<PredictionResult> findTopByCompanyIdOrderByCalculatedAtDesc(String companyId);
}
