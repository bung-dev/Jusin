package com.jusin.scheduler;

import com.jusin.domain.entity.FinancialStatement;
import com.jusin.repository.FinancialIndicatorRepository;
import com.jusin.repository.FinancialStatementRepository;
import com.jusin.service.IndicatorCalculationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IndicatorCalculationScheduler {

    private final FinancialStatementRepository fsRepository;
    private final IndicatorCalculationService indicatorService;
    private final FinancialIndicatorRepository indicatorRepository;

    @PostConstruct
    public void init() {
        recalculateIndicators();
        recalculatePredictions();
    }

    /**
     * 매일 오전 7시 - 신규 재무제표 지표 재계산
     */
    @Scheduled(cron = "0 0 7 * * *")
    public void recalculateIndicators() {
        log.info("[스케줄러] 지표 재계산 시작");

        List<FinancialStatement> statements = fsRepository.findStatementsWithoutIndicators();
        log.info("재계산 대상: {}건", statements.size());

        int success = 0, failed = 0;
        for (FinancialStatement fs : statements) {
            try {
                indicatorService.calculateAndSave(fs.getCompanyId(), fs.getPeriod());
                success++;
                log.debug("지표 계산 완료: {}/{}", fs.getCompanyId(), fs.getPeriod());
            } catch (Exception e) {
                failed++;
                log.warn("지표 계산 실패: {}/{} - {}", fs.getCompanyId(), fs.getPeriod(), e.getMessage());
            }
        }

        log.info("[스케줄러] 지표 재계산 완료 - 성공: {}, 실패: {}", success, failed);
    }

    /**
     * 매일 오전 7시 30분 - 예측 결과 재계산 (stub)
     */
    @Scheduled(cron = "0 30 7 * * *")
    public void recalculatePredictions() {
        log.info("[스케줄러] 예측 재계산 시작");
        long indicatorCount = indicatorRepository.count();
        log.info("[스케줄러] 예측 재계산 완료 - 지표 총 {}건", indicatorCount);
    }
}
