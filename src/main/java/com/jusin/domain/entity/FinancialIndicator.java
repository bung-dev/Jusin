package com.jusin.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_indicators")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FinancialIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, length = 8)
    private String companyId;

    @Column(nullable = false, length = 10)
    private String period;

    private BigDecimal per;
    private BigDecimal roe;
    private BigDecimal debtRatio;
    private BigDecimal eps;
    private BigDecimal epsGrowth;
    private BigDecimal pbr;
    private BigDecimal operatingMargin;
    private BigDecimal currentRatio;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @Builder
    public FinancialIndicator(String companyId, String period, BigDecimal per,
                               BigDecimal roe, BigDecimal debtRatio, BigDecimal eps,
                               BigDecimal epsGrowth, BigDecimal pbr,
                               BigDecimal operatingMargin, BigDecimal currentRatio) {
        this.companyId = companyId;
        this.period = period;
        this.per = per;
        this.roe = roe;
        this.debtRatio = debtRatio;
        this.eps = eps;
        this.epsGrowth = epsGrowth;
        this.pbr = pbr;
        this.operatingMargin = operatingMargin;
        this.currentRatio = currentRatio;
        this.calculatedAt = LocalDateTime.now();
    }

    public void update(FinancialIndicator other) {
        this.per = other.per;
        this.roe = other.roe;
        this.debtRatio = other.debtRatio;
        this.eps = other.eps;
        this.epsGrowth = other.epsGrowth;
        this.pbr = other.pbr;
        this.operatingMargin = other.operatingMargin;
        this.currentRatio = other.currentRatio;
        this.calculatedAt = LocalDateTime.now();
    }
}
