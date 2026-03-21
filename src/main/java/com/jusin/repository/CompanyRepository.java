package com.jusin.repository;

import com.jusin.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByStockCode(String stockCode);
    List<Company> findByCompanyNameContainingIgnoreCase(String name);
    Optional<Company> findByCompanyId(String companyId);
}
