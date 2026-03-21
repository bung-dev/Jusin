package com.jusin.repository;

import com.jusin.domain.entity.FinancialIndicator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialIndicatorRepository extends JpaRepository<FinancialIndicator, Long> {
    Optional<FinancialIndicator> findByCompanyIdAndPeriod(String companyId, String period);
    List<FinancialIndicator> findByCompanyIdOrderByPeriodDesc(String companyId);
}
