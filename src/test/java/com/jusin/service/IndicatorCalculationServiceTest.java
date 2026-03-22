package com.jusin.service;

import com.jusin.domain.entity.FinancialStatement;
import com.jusin.fixture.FinancialStatementFixture;
import com.jusin.repository.FinancialIndicatorRepository;
import com.jusin.repository.FinancialStatementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndicatorCalculationServiceTest {

    @Mock
    private FinancialStatementRepository fsRepository;

    @Mock
    private FinancialIndicatorRepository indicatorRepository;

    @InjectMocks
    private IndicatorCalculationService service;

    private final FinancialStatement fs     = FinancialStatementFixture.samsung2025Q4();
    private final FinancialStatement prevFs = FinancialStatementFixture.samsung2024Q4();

    @Test
    @DisplayName("ROE 계산 - 삼성전자 2025Q4: 12,206억 / 223,022억 × 100 ≈ 5.47%")
    void roe_samsung2025Q4() {
        BigDecimal roe = service.calculateRoe(fs.getNetIncome(), fs.getEquity());
        assertThat(roe).isEqualByComparingTo(new BigDecimal("5.47"));
    }

    @Test
    @DisplayName("부채비율 계산 - 삼성전자 2025Q4: 177,512억 / 223,022억 × 100 ≈ 79.59%")
    void debtRatio_samsung2025Q4() {
        BigDecimal dr = service.calculateDebtRatio(fs.getTotalLiabilities(), fs.getEquity());
        assertThat(dr).isEqualByComparingTo(new BigDecimal("79.59"));
    }

    @Test
    @DisplayName("EPS 계산 - 삼성전자 2025Q4: 12,206억 / 27억 주 ≈ 4,521원")
    void eps_samsung2025Q4() {
        BigDecimal eps = service.calculateEps(fs.getNetIncome(), fs.getShareCount());
        assertThat(eps).isEqualByComparingTo(new BigDecimal("4521"));
    }

    @Test
    @DisplayName("EPS YoY 증가율: 현재 4,521원 vs 전년 2,963원 → 50% 이상 증가")
    void epsGrowth_samsung2025Q4() {
        BigDecimal currentEps = service.calculateEps(fs.getNetIncome(), fs.getShareCount());
        BigDecimal prevEps    = service.calculateEps(prevFs.getNetIncome(), prevFs.getShareCount());
        BigDecimal growth     = service.calculateEpsGrowth(currentEps, prevEps);
        assertThat(growth).isGreaterThan(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("영업이익률: 14,299억 / 77,284억 × 100 ≈ 18.50%")
    void operatingMargin_samsung2025Q4() {
        BigDecimal margin = service.calculateOperatingMargin(
                fs.getOperatingIncome(), fs.getRevenue());
        assertThat(margin).isEqualByComparingTo(new BigDecimal("18.50"));
    }

    @Test
    @DisplayName("유동비율: 150,000억 / 83,000억 ≈ 1.81")
    void currentRatio_samsung2025Q4() {
        BigDecimal cr = service.calculateCurrentRatio(
                fs.getCurrentAssets(), fs.getCurrentLiabilities());
        assertThat(cr).isEqualByComparingTo(new BigDecimal("1.81"));
    }

    @Test
    @DisplayName("자기자본 0일 때 ROE null 반환")
    void calculateRoe_zeroEquity() {
        assertThat(service.calculateRoe(new BigDecimal("100"), BigDecimal.ZERO)).isNull();
    }

    @Test
    @DisplayName("null 입력 시 NPE 없이 null 반환")
    void nullSafety() {
        assertAll(
            () -> assertThat(service.calculateRoe(null, null)).isNull(),
            () -> assertThat(service.calculateDebtRatio(null, BigDecimal.ZERO)).isNull(),
            () -> assertThat(service.calculateEps(null, 0L)).isNull(),
            () -> assertThat(service.calculateOperatingMargin(null, null)).isNull(),
            () -> assertThat(service.calculateCurrentRatio(null, null)).isNull()
        );
    }
}
