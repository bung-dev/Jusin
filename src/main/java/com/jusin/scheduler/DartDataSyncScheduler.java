package com.jusin.scheduler;

import com.jusin.client.DartApiRateLimiter;
import com.jusin.domain.entity.Company;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.domain.enums.SyncType;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.repository.CompanyRepository;
import com.jusin.service.AdminSyncService;
import com.jusin.service.DataSyncLogService;
import com.jusin.service.FinancialStatementService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DartDataSyncScheduler {

    private final CompanyRepository companyRepository;
    private final FinancialStatementService fsService;
    private final DataSyncLogService syncLogService;
    private final DartApiRateLimiter rateLimiter;
    private final AdminSyncService adminSyncService;


    /**
     * 매일 오전 6시 - 전체 기업 재무제표 동기화
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void syncAllCompanies() {
        log.info("[스케줄러] 전체 기업 재무제표 동기화 시작");
        int success = 0, failed = 0;

        List<Company> companies = companyRepository.findAll();
        log.info("대상 기업 수: {}", companies.size());

        for (Company company : companies) {
            if (!rateLimiter.canCall()) {
                log.warn("[스케줄러] DART API 일일 호출 한도 초과, 중단");
                break;
            }

            try {
                List<FinancialStatement> saved = fsService.collectAndSave(company.getStockCode());
                if (!saved.isEmpty()) {
                    syncLogService.logSuccess(company.getCompanyId(), SyncType.FINANCIAL, saved.size());
                    success++;
                    rateLimiter.incrementCount();
                    log.debug("동기화 완료: {}", company.getCompanyName());
                }
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("스케줄러 인터럽트: {}", company.getCompanyName());
            } catch (Exception e) {
                failed++;
                syncLogService.logFailure(company.getCompanyId(), SyncType.FINANCIAL, e.getMessage());
                log.warn("동기화 실패: {}, error={}", company.getCompanyName(), e.getMessage());
            }
        }

        log.info("[스케줄러] 동기화 완료 - 성공: {}, 실패: {}", success, failed);
    }

    /**
     * 매주 일요일 오전 2시 - 기업 정보 갱신
     * TODO: 기업 정보 갱신 미구현 — 구현 완료 전까지 비활성화
     */
    // TODO: DART company.json API로 전체 기업 정보 갱신 구현 예정
    // @Scheduled(cron = "0 0 2 * * SUN")
    public void syncCompanyInfo() {
        // TODO: DART company.json API로 전체 기업 정보 갱신 구현 예정
        log.info("[스케줄러] 기업 정보 갱신 시작 (대상: {} 개)", companyRepository.count());
    }

    /**
     * 매월 1일 오전 2시 - 기업코드 재동기화
     */
    @Scheduled(cron = "0 0 2 1 * *")
    public void scheduleCorpCodeSync() {
        log.info("[스케줄러] 월간 기업코드 재동기화 시작");
        try {
            CorpCodeSyncResponse result = adminSyncService.syncCorpCodes();
            syncLogService.logSuccess("SYSTEM", SyncType.CORP_CODE, result.total());
            log.info("[스케줄러] 월간 기업코드 재동기화 완료 - total={}, created={}, updated={}",
                    result.total(), result.created(), result.updated());
        } catch (Exception e) {
            syncLogService.logFailure("SYSTEM", SyncType.CORP_CODE, e.getMessage());
            log.error("[스케줄러] 월간 기업코드 재동기화 실패", e);
        }
    }
}
