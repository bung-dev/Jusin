package com.jusin.service;

import com.jusin.domain.entity.FinancialIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class IndicatorValidator {

    private static final BigDecimal ROE_WARN_THRESHOLD        = BigDecimal.valueOf(200);
    private static final BigDecimal DEBT_RATIO_WARN_THRESHOLD = BigDecimal.valueOf(1000);
    private static final BigDecimal MARGIN_WARN_THRESHOLD     = BigDecimal.valueOf(100);

    public void validate(FinancialIndicator indicator) {
        if (indicator.getRoe() != null
                && indicator.getRoe().abs().compareTo(ROE_WARN_THRESHOLD) > 0) {
            log.warn("[이상치 경고] ROE 극단값: companyId={}, roe={}",
                    indicator.getCompanyId(), indicator.getRoe());
        }
        if (indicator.getDebtRatio() != null
                && indicator.getDebtRatio().compareTo(DEBT_RATIO_WARN_THRESHOLD) > 0) {
            log.warn("[이상치 경고] 부채비율 극단값: companyId={}, debtRatio={}",
                    indicator.getCompanyId(), indicator.getDebtRatio());
        }
        if (indicator.getOperatingMargin() != null
                && indicator.getOperatingMargin().abs().compareTo(MARGIN_WARN_THRESHOLD) > 0) {
            log.warn("[이상치 경고] 영업이익률 극단값: companyId={}, margin={}",
                    indicator.getCompanyId(), indicator.getOperatingMargin());
        }
    }
}
