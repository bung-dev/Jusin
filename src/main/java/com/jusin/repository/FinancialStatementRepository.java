package com.jusin.repository;

import com.jusin.domain.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {
    List<FinancialStatement> findByCompanyIdOrderByPeriodDesc(String companyId);
    Optional<FinancialStatement> findByCompanyIdAndPeriod(String companyId, String period);
    List<FinancialStatement> findByCompanyIdAndPeriodIn(String companyId, List<String> periods);
}
