package com.jusin.service;

import com.jusin.domain.entity.Company;
import com.jusin.dto.response.AnalysisResponse;
import com.jusin.util.PeriodParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompareAnalysisService {

    private final CompanyService companyService;
    private final FinancialStatementService fsService;
    private final PredictionService predictionService;

    /**
     * 두 기업의 분석 결과를 나란히 반환
     * @param codes 2개 종목코드 목록
     * @return 두 기업의 AnalysisResponse 리스트
     */
    public List<AnalysisResponse> compareAnalysis(List<String> codes) {
        String period = PeriodParseUtil.resolveLatestPeriod();

        CompletableFuture<AnalysisResponse> future0 =
                CompletableFuture.supplyAsync(() -> analyzeOne(codes.get(0), period));
        CompletableFuture<AnalysisResponse> future1 =
                CompletableFuture.supplyAsync(() -> analyzeOne(codes.get(1), period));

        try {
            return List.of(future0.get(), future1.get());
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private AnalysisResponse analyzeOne(String stockCode, String period) {
        Company company = companyService.getOrFetchCompany(stockCode);
        fsService.collectAndSave(stockCode);
        return predictionService.analyzeAndPredict(stockCode, company.getCompanyId(), period);
    }
}
