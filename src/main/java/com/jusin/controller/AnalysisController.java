package com.jusin.controller;

import com.jusin.domain.entity.Company;
import com.jusin.dto.response.AnalysisResponse;
import com.jusin.dto.response.ApiResponse;
import com.jusin.dto.response.QuarterlyHistoryResponse;
import com.jusin.dto.response.SignalHistoryResponse;
import com.jusin.service.CompanyService;
import com.jusin.service.FinancialStatementService;
import com.jusin.service.PredictionService;
import com.jusin.service.CompareAnalysisService;
import com.jusin.service.SignalHistoryService;
import com.jusin.util.PeriodParseUtil;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AnalysisController {

    private final CompanyService companyService;
    private final FinancialStatementService fsService;
    private final PredictionService predictionService;
    private final SignalHistoryService signalHistoryService;
    private final CompareAnalysisService compareAnalysisService;

    /**
     * 두 기업 비교 분석
     * GET /api/v1/analysis/compare?codes=005930,000660
     */
    @GetMapping("/compare")
    public ResponseEntity<ApiResponse<List<AnalysisResponse>>> compare(
            @RequestParam String codes) {

        log.info("비교 분석 요청: codes={}", codes);

        List<String> codeArray = validateAndParseCodes(codes);
        List<AnalysisResponse> responses = compareAnalysisService.compareAnalysis(codeArray);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private List<String> validateAndParseCodes(String codes) {
        if (codes == null || codes.isBlank()) {
            throw new IllegalArgumentException("codes 파라미터는 필수입니다.");
        }
        String[] parts = codes.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("codes 파라미터는 정확히 2개의 6자리 숫자 종목코드를 쉼표로 구분하여 입력해주세요.");
        }
        for (String code : parts) {
            if (!code.trim().matches("\\d{6}")) {
                throw new IllegalArgumentException("codes 파라미터는 정확히 2개의 6자리 숫자 종목코드를 쉼표로 구분하여 입력해주세요.");
            }
        }
        if (parts[0].trim().equals(parts[1].trim())) {
            throw new IllegalArgumentException("두 종목코드는 서로 달라야 합니다.");
        }
        return List.of(parts[0].trim(), parts[1].trim());
    }

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
        String latestPeriod = PeriodParseUtil.resolveLatestPeriod();

        // 4. 예측 결과 (캐시 or 계산)
        AnalysisResponse response = predictionService.analyzeAndPredict(
                stockCode, company.getCompanyId(), latestPeriod);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{stockCode}/history")
    public ResponseEntity<ApiResponse<SignalHistoryResponse>> getSignalHistory(
            @PathVariable
            @Pattern(regexp = "\\d{6}", message = "종목코드는 6자리 숫자여야 합니다.")
            String stockCode,
            @RequestParam(required = false, defaultValue = "1m") String period) {

        log.info("신호 이력 조회 요청: stockCode={}, period={}", stockCode, period);
        SignalHistoryResponse response = signalHistoryService.getSignalHistory(stockCode, period);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{stockCode}/quarterly")
    public ResponseEntity<ApiResponse<QuarterlyHistoryResponse>> getQuarterlyHistory(
            @PathVariable
            @Pattern(regexp = "\\d{6}", message = "종목코드는 6자리 숫자여야 합니다.")
            String stockCode,
            @RequestParam(defaultValue = "8") int quarters) {

        log.info("분기별 히스토리 조회 요청: stockCode={}, quarters={}", stockCode, quarters);
        QuarterlyHistoryResponse response = signalHistoryService.getQuarterlyHistory(stockCode, quarters);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
