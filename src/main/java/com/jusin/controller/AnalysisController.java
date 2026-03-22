package com.jusin.controller;

import com.jusin.domain.entity.Company;
import com.jusin.dto.response.AnalysisResponse;
import com.jusin.dto.response.ApiResponse;
import com.jusin.service.CompanyService;
import com.jusin.service.FinancialStatementService;
import com.jusin.service.PredictionService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AnalysisController {

    private final CompanyService companyService;
    private final FinancialStatementService fsService;
    private final PredictionService predictionService;

    /**
     * 재무 분석 및 예측 결과 조회
     * GET /api/v1/analysis/{stockCode}
     */
    @GetMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> analyze(
            @PathVariable
            @Pattern(regexp = "\\d{6}", message = "종목코드는 6자리 숫자여야 합니다.")
            String stockCode) {

        log.info("분석 요청: stockCode={}", stockCode);

        // 1. 기업 조회 (없으면 DART에서 가져옴)
        Company company = companyService.getOrFetchCompany(stockCode);

        // 2. 재무제표 수집
        fsService.collectAndSave(stockCode);

        // 3. 최신 기간 결정
        String latestPeriod = resolveLatestPeriod();

        // 4. 예측 결과 (캐시 or 계산)
        AnalysisResponse response = predictionService.analyzeAndPredict(
                stockCode, company.getCompanyId(), latestPeriod);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String resolveLatestPeriod() {
        YearMonth reportQuarter = YearMonth.now().minusMonths(3);
        int rq = (reportQuarter.getMonthValue() - 1) / 3 + 1;
        return reportQuarter.getYear() + "-Q" + rq;
    }
}
