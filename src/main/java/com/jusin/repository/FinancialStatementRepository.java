package com.jusin.repository;

import com.jusin.domain.entity.FinancialStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {
    List<FinancialStatement> findByCompanyIdOrderByPeriodDesc(String companyId);
    Optional<FinancialStatement> findByCompanyIdAndPeriod(String companyId, String period);
    List<FinancialStatement> findByCompanyIdAndPeriodIn(String companyId, List<String> periods);

    @Query("""
        SELECT fs FROM FinancialStatement fs
        WHERE NOT EXISTS (
            SELECT 1 FROM FinancialIndicator fi
            WHERE fi.companyId = fs.companyId
              AND fi.period    = fs.period
        )
        ORDER BY fs.period DESC
    """)
    List<FinancialStatement> findStatementsWithoutIndicators();
}
