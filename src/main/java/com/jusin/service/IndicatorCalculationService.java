package com.jusin.service;

import com.jusin.client.StockPriceClient;
import com.jusin.domain.entity.FinancialIndicator;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.exception.InsufficientDataException;
import com.jusin.repository.CompanyRepository;
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
    private final StockPriceClient stockPriceClient;
    private final CompanyRepository companyRepository;

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    @Transactional
    public FinancialIndicator calculateAndSave(String companyId, String period) {
        FinancialStatement fs = fsRepository
                .findByCompanyIdAndPeriod(companyId, period)
                .orElseThrow(() -> new InsufficientDataException(
                        companyId + " / " + period + " 재무제표 없음"));

        String stockCode = companyRepository.findByCompanyId(companyId)
                .map(com.jusin.domain.entity.Company::getStockCode)
                .orElse(null);
        StockPriceClient.NaverStockInfo stockInfo = stockCode != null
                ? getStockInfoSafely(stockCode)
                : new StockPriceClient.NaverStockInfo(null, null);
        BigDecimal currentPrice = stockInfo.price();

        Long rawShareCount = fs.getShareCount();
        if (rawShareCount == null) {
            rawShareCount = stockInfo.shareCount();
        }
        final Long shareCount = rawShareCount;

        String prevPeriod = getPreviousPeriod(period);
        BigDecimal prevEps = fsRepository.findByCompanyIdAndPeriod(companyId, prevPeriod)
                .map(prev -> calculateEps(prev.getNetIncome(),
                        prev.getShareCount() != null ? prev.getShareCount() : shareCount))
                .orElse(null);

        BigDecimal currentEps = calculateEps(fs.getNetIncome(), shareCount);

        FinancialIndicator indicator = FinancialIndicator.builder()
                .companyId(companyId)
                .period(period)
                .per(calculatePer(currentPrice, currentEps))
                .roe(calculateRoe(fs.getNetIncome(), fs.getEquity()))
                .debtRatio(calculateDebtRatio(fs.getTotalLiabilities(), fs.getEquity()))
                .eps(currentEps)
                .epsGrowth(calculateEpsGrowth(currentEps, prevEps))
                .pbr(calculatePbr(currentPrice, fs.getEquity(), shareCount))
                .operatingMargin(calculateOperatingMargin(fs.getOperatingIncome(), fs.getRevenue()))
                .currentRatio(calculateCurrentRatio(fs.getCurrentAssets(), fs.getCurrentLiabilities()))
                .build();

        return indicatorRepository.findByCompanyIdAndPeriod(companyId, period)
                .map(existing -> {
                    existing.update(indicator);
                    return indicatorRepository.save(existing);
                })
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

    public BigDecimal calculatePer(BigDecimal currentPrice, BigDecimal eps) {
        if (currentPrice == null || eps == null || eps.compareTo(BigDecimal.ZERO) <= 0) return null;
        return currentPrice.divide(eps, SCALE, ROUNDING);
    }

    public BigDecimal calculatePbr(BigDecimal currentPrice, BigDecimal equity, Long shareCount) {
        if (currentPrice == null || equity == null || shareCount == null || shareCount == 0) return null;
        BigDecimal bps = equity.divide(BigDecimal.valueOf(shareCount), SCALE, ROUNDING);
        if (bps.compareTo(BigDecimal.ZERO) <= 0) return null;
        return currentPrice.divide(bps, SCALE, ROUNDING);
    }

    private StockPriceClient.NaverStockInfo getStockInfoSafely(String stockCode) {
        try {
            return stockPriceClient.getStockInfo(stockCode);
        } catch (Exception e) {
            log.warn("주가/주식수 조회 실패, PER/PBR/EPS null 처리: stockCode={}, error={}", stockCode, e.getMessage());
            return new StockPriceClient.NaverStockInfo(null, null);
        }
    }

    private String getPreviousPeriod(String period) {
        int year = Integer.parseInt(period.substring(0, 4));
        String suffix = period.substring(4);
        return (year - 1) + suffix;
    }
}
