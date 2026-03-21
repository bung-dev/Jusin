package com.jusin.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_statements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 8)
    private String companyId;

    @Column(nullable = false, length = 10)
    private String period;

    @Column(name = "statement_type", length = 20)
    private String statementType;

    // 손익계산서
    private BigDecimal revenue;
    private BigDecimal costOfSales;
    private BigDecimal grossProfit;
    private BigDecimal operatingExpenses;
    private BigDecimal operatingIncome;
    private BigDecimal nonOperatingIncome;
    private BigDecimal nonOperatingExpense;
    private BigDecimal incomeTaxExpense;
    private BigDecimal netIncome;
    private Long shareCount;

    // 재무상태표
    private BigDecimal currentAssets;
    private BigDecimal totalAssets;
    private BigDecimal currentLiabilities;
    private BigDecimal totalLiabilities;
    private BigDecimal equity;

    // 현금흐름표
    private BigDecimal operatingCashFlow;
    private BigDecimal investingCashFlow;
    private BigDecimal financingCashFlow;

    @Column(name = "dart_report_code", length = 50)
    private String dartReportCode;

    @Column(name = "reported_date")
    private LocalDate reportedDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public FinancialStatement(String companyId, String period, String statementType,
                               BigDecimal revenue, BigDecimal operatingIncome,
                               BigDecimal netIncome, Long shareCount,
                               BigDecimal currentAssets, BigDecimal totalAssets,
                               BigDecimal currentLiabilities, BigDecimal totalLiabilities,
                               BigDecimal equity, String dartReportCode, LocalDate reportedDate) {
        this.companyId = companyId;
        this.period = period;
        this.statementType = statementType;
        this.revenue = revenue;
        this.operatingIncome = operatingIncome;
        this.netIncome = netIncome;
        this.shareCount = shareCount;
        this.currentAssets = currentAssets;
        this.totalAssets = totalAssets;
        this.currentLiabilities = currentLiabilities;
        this.totalLiabilities = totalLiabilities;
        this.equity = equity;
        this.dartReportCode = dartReportCode;
        this.reportedDate = reportedDate;
    }
}
