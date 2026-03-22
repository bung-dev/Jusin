package com.jusin.service;

import com.jusin.domain.entity.FinancialIndicator;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.exception.InsufficientDataException;
import com.jusin.repository.FinancialIndicatorRepository;
import com.jusin.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicatorCalculationService {

    private final FinancialStatementRepository fsRepository;
    private final FinancialIndicatorRepository indicatorRepository;

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Transactional
    public FinancialIndicator calculateAndSave(String companyId, String period) {
        FinancialStatement fs = fsRepository
                .findByCompanyIdAndPeriod(companyId, period)
                .orElseThrow(() -> new InsufficientDataException(
                        companyId + " / " + period + " 재무제표 없음"));

        String prevPeriod = getPreviousPeriod(period);
        BigDecimal prevEps = fsRepository.findByCompanyIdAndPeriod(companyId, prevPeriod)
                .map(prev -> calculateEps(prev.getNetIncome(), prev.getShareCount()))
                .orElse(null);

        BigDecimal currentEps = calculateEps(fs.getNetIncome(), fs.getShareCount());

        FinancialIndicator indicator = FinancialIndicator.builder()
                .companyId(companyId)
                .period(period)
                .per(null)
                .roe(calculateRoe(fs.getNetIncome(), fs.getEquity()))
                .debtRatio(calculateDebtRatio(fs.getTotalLiabilities(), fs.getEquity()))
                .eps(currentEps)
                .epsGrowth(calculateEpsGrowth(currentEps, prevEps))
                .pbr(null)
                .operatingMargin(calculateOperatingMargin(fs.getOperatingIncome(), fs.getRevenue()))
                .currentRatio(calculateCurrentRatio(fs.getCurrentAssets(), fs.getCurrentLiabilities()))
                .build();

        return indicatorRepository.findByCompanyIdAndPeriod(companyId, period)
                .map(existing -> indicatorRepository.save(indicator))
                .orElseGet(() -> indicatorRepository.save(indicator));
    }

    public BigDecimal calculateRoe(BigDecimal netIncome, BigDecimal equity) {
        if (netIncome == null || equity == null || equity.compareTo(BigDecimal.ZERO) == 0) {
            log.debug("ROE 계산 불가: 데이터 부족");
            return null;
        }
        return netIncome.multiply(BigDecimal.valueOf(100))
                .divide(equity, SCALE, ROUNDING);
    }

    public BigDecimal calculateDebtRatio(BigDecimal totalLiabilities, BigDecimal equity) {
        if (totalLiabilities == null || equity == null || equity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return totalLiabilities.multiply(BigDecimal.valueOf(100))
                .divide(equity, SCALE, ROUNDING);
    }

    public BigDecimal calculateEps(BigDecimal netIncome, Long shareCount) {
        if (netIncome == null || shareCount == null || shareCount == 0) {
            return null;
        }
        return netIncome.divide(BigDecimal.valueOf(shareCount), 0, ROUNDING);
    }

    public BigDecimal calculateEpsGrowth(BigDecimal currentEps, BigDecimal prevEps) {
        if (currentEps == null || prevEps == null
                || prevEps.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentEps.subtract(prevEps)
                .multiply(BigDecimal.valueOf(100))
                .divide(prevEps.abs(), SCALE, ROUNDING);
    }

    public BigDecimal calculateOperatingMargin(BigDecimal operatingIncome, BigDecimal revenue) {
        if (operatingIncome == null || revenue == null
                || revenue.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return operatingIncome.multiply(BigDecimal.valueOf(100))
                .divide(revenue, SCALE, ROUNDING);
    }

    public BigDecimal calculateCurrentRatio(BigDecimal currentAssets, BigDecimal currentLiabilities) {
        if (currentAssets == null || currentLiabilities == null
                || currentLiabilities.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return currentAssets.divide(currentLiabilities, SCALE, ROUNDING);
    }

    private String getPreviousPeriod(String period) {
        int year = Integer.parseInt(period.substring(0, 4));
        String suffix = period.substring(4);
        return (year - 1) + suffix;
    }
}
