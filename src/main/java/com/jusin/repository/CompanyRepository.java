package com.jusin.repository;

import com.jusin.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByStockCode(String stockCode);
    List<Company> findByCompanyNameContainingIgnoreCase(String name);
    Optional<Company> findByCompanyId(String companyId);

    @Modifying
    @Query(value = """
            INSERT INTO companies (company_id, company_name, stock_code)
            VALUES (:companyId, :companyName, :stockCode)
            ON DUPLICATE KEY UPDATE company_name = VALUES(company_name)
            """, nativeQuery = true)
    void upsertCompany(@Param("companyId") String companyId,
                       @Param("companyName") String companyName,
                       @Param("stockCode") String stockCode);
}
