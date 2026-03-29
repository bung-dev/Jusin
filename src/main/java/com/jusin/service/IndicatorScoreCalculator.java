package com.jusin.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IndicatorScoreCalculator {

    private static final BigDecimal ZERO      = BigDecimal.valueOf(0);
    private static final BigDecimal ONE       = BigDecimal.valueOf(1.0);
    private static final BigDecimal ONE_POINT_TWO  = BigDecimal.valueOf(1.2);
    private static final BigDecimal ONE_POINT_FIVE = BigDecimal.valueOf(1.5);
    private static final BigDecimal TWO       = BigDecimal.valueOf(2.0);
    private static final BigDecimal TWO_POINT_FIVE = BigDecimal.valueOf(2.5);
    private static final BigDecimal THREE     = BigDecimal.valueOf(3.0);
    private static final BigDecimal FIVE      = BigDecimal.valueOf(5);
    private static final BigDecimal TEN       = BigDecimal.valueOf(10);
    private static final BigDecimal NEG_TEN   = BigDecimal.valueOf(-10);
    private static final BigDecimal FIFTEEN   = BigDecimal.valueOf(15);
    private static final BigDecimal TWENTY    = BigDecimal.valueOf(20);
    private static final BigDecimal THIRTY    = BigDecimal.valueOf(30);
    private static final BigDecimal FIFTY     = BigDecimal.valueOf(50);
    private static final BigDecimal HUNDRED   = BigDecimal.valueOf(100);
    private static final BigDecimal HUNDRED_FIFTY = BigDecimal.valueOf(150);

    public int scorePer(BigDecimal per) {
        if (per == null) return 0;
        if (per.compareTo(TEN) <= 0)    return 20;
        if (per.compareTo(TWENTY) <= 0) return 15;
        if (per.compareTo(THIRTY) <= 0) return 10;
        return 0;
    }

    public int scoreRoe(BigDecimal roe) {
        if (roe == null) return 0;
        if (roe.compareTo(FIFTEEN) >= 0) return 20;
        if (roe.compareTo(TEN) >= 0)     return 15;
        if (roe.compareTo(FIVE) >= 0)    return 10;
        if (roe.compareTo(ZERO) >= 0)    return 5;
        return 0;
    }

    public int scoreDebtRatio(BigDecimal debtRatio) {
        if (debtRatio == null) return 0;
        if (debtRatio.compareTo(FIFTY) <= 0)         return 15;
        if (debtRatio.compareTo(HUNDRED) <= 0)       return 10;
        if (debtRatio.compareTo(HUNDRED_FIFTY) <= 0) return 5;
        return 0;
    }

    public int scoreEpsGrowth(BigDecimal epsGrowth) {
        if (epsGrowth == null) return 0;
        if (epsGrowth.compareTo(TEN) >= 0)    return 15;
        if (epsGrowth.compareTo(ZERO) >= 0)   return 10;
        if (epsGrowth.compareTo(NEG_TEN) >= 0) return 5;
        return 0;
    }

    public int scorePbr(BigDecimal pbr) {
        if (pbr == null) return 0;
        if (pbr.compareTo(ONE) <= 0)   return 10;
        if (pbr.compareTo(TWO) <= 0)   return 8;
        if (pbr.compareTo(THREE) <= 0) return 5;
        return 0;
    }

    public int scoreOperatingMargin(BigDecimal margin) {
        if (margin == null) return 0;
        if (margin.compareTo(FIFTEEN) >= 0) return 10;
        if (margin.compareTo(TEN) >= 0)     return 8;
        if (margin.compareTo(FIVE) >= 0)    return 5;
        if (margin.compareTo(ZERO) >= 0)    return 2;
        return 0;
    }

    public int scoreCurrentRatio(BigDecimal currentRatio) {
        if (currentRatio == null) return 0;
        if (currentRatio.compareTo(ONE_POINT_FIVE) >= 0 && currentRatio.compareTo(TWO) <= 0)             return 10;
        if (currentRatio.compareTo(ONE_POINT_TWO) >= 0 && currentRatio.compareTo(TWO_POINT_FIVE) <= 0)   return 8;
        if (currentRatio.compareTo(ONE) >= 0 && currentRatio.compareTo(THREE) <= 0)                      return 5;
        if (currentRatio.compareTo(ONE) < 0)                                                              return 0;
        return 5;
    }
}
