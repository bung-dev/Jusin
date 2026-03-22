package com.jusin.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class IndicatorScoreCalculator {

    public int scorePer(BigDecimal per) {
        if (per == null) return 0;
        double v = per.doubleValue();
        if (v <= 10) return 20;
        if (v <= 20) return 15;
        if (v <= 30) return 10;
        return 0;
    }

    public int scoreRoe(BigDecimal roe) {
        if (roe == null) return 0;
        double v = roe.doubleValue();
        if (v >= 15) return 20;
        if (v >= 10) return 15;
        if (v >= 5)  return 10;
        if (v >= 0)  return 5;
        return 0;
    }

    public int scoreDebtRatio(BigDecimal debtRatio) {
        if (debtRatio == null) return 0;
        double v = debtRatio.doubleValue();
        if (v <= 50)  return 15;
        if (v <= 100) return 10;
        if (v <= 150) return 5;
        return 0;
    }

    public int scoreEpsGrowth(BigDecimal epsGrowth) {
        if (epsGrowth == null) return 0;
        double v = epsGrowth.doubleValue();
        if (v >= 10)  return 15;
        if (v >= 0)   return 10;
        if (v >= -10) return 5;
        return 0;
    }

    public int scorePbr(BigDecimal pbr) {
        if (pbr == null) return 0;
        double v = pbr.doubleValue();
        if (v <= 1.0) return 10;
        if (v <= 2.0) return 8;
        if (v <= 3.0) return 5;
        return 0;
    }

    public int scoreOperatingMargin(BigDecimal margin) {
        if (margin == null) return 0;
        double v = margin.doubleValue();
        if (v >= 15) return 10;
        if (v >= 10) return 8;
        if (v >= 5)  return 5;
        if (v >= 0)  return 2;
        return 0;
    }

    public int scoreCurrentRatio(BigDecimal currentRatio) {
        if (currentRatio == null) return 0;
        double v = currentRatio.doubleValue();
        if (v >= 1.5 && v <= 2.0) return 10;
        if (v >= 1.2 && v <= 2.5) return 8;
        if (v >= 1.0 && v <= 3.0) return 5;
        if (v < 1.0)              return 0;
        return 5;
    }
}
