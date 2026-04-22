package com.jusin.controller;

import com.jusin.client.DartApiRateLimiter;
import com.jusin.dto.response.AdminStatsResponse;
import com.jusin.dto.response.ApiResponse;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.dto.response.HistoricalSyncResponse;
import com.jusin.dto.response.SyncLogPageResponse;
import com.jusin.repository.CompanyRepository;
import com.jusin.repository.DataSyncLogRepository;
import com.jusin.repository.FinancialStatementRepository;
import com.jusin.repository.PredictionResultRepository;
import com.jusin.service.AdminSyncService;
import com.jusin.service.DataSyncLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminSyncService adminSyncService;
    private final DataSyncLogService dataSyncLogService;
    private final CompanyRepository companyRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final PredictionResultRepository predictionResultRepository;
    private final DataSyncLogRepository dataSyncLogRepository;
    private final DartApiRateLimiter dartApiRateLimiter;

    @GetMapping("/sync/corp-codes")
    public ResponseEntity<ApiResponse<CorpCodeSyncResponse>> syncCorpCodes() {
        log.info("기업코드 전체 동기화 요청");
        CorpCodeSyncResponse result = adminSyncService.syncCorpCodes();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/sync/historical")
    public ResponseEntity<ApiResponse<HistoricalSyncResponse>> syncHistorical(
            @RequestParam String stockCode,
            @RequestParam(defaultValue = "8") int quarters) {
        log.info("소급 수집 요청: stockCode={}, quarters={}", stockCode, quarters);
        HistoricalSyncResponse result = adminSyncService.syncHistorical(stockCode, quarters);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/sync/historical/all")
    public ResponseEntity<ApiResponse<HistoricalSyncResponse>> syncHistoricalAll(
            @RequestParam(defaultValue = "8") int quarters) {
        log.info("전체 기업 소급 수집 요청: quarters={}", quarters);
        HistoricalSyncResponse result = adminSyncService.syncHistoricalAll(quarters);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/sync/logs")
    public ResponseEntity<ApiResponse<SyncLogPageResponse>> getSyncLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String syncType) {
        SyncLogPageResponse result = dataSyncLogService.getLogs(page, size, status, syncType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        AdminStatsResponse stats = AdminStatsResponse.builder()
                .totalCompanies(companyRepository.count())
                .totalFinancialStatements(financialStatementRepository.count())
                .totalPredictionResults(predictionResultRepository.count())
                .todaySyncCount(dataSyncLogRepository.countTodaySuccessSync(startOfDay, endOfDay))
                .dartApiDailyLimit(9000)
                .dartApiUsedToday(dartApiRateLimiter.getDailyUsedCount())
                .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
