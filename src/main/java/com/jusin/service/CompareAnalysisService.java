package com.jusin.service;

import com.jusin.domain.entity.Company;
import com.jusin.dto.response.AnalysisResponse;
import com.jusin.util.PeriodParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        return List.of(
                analyzeOne(codes.get(0), period),
                analyzeOne(codes.get(1), period)
        );
    }

    private AnalysisResponse analyzeOne(String stockCode, String period) {
        Company company = companyService.getOrFetchCompany(stockCode);
        fsService.collectAndSave(stockCode);
        return predictionService.analyzeAndPredict(stockCode, company.getCompanyId(), period);
    }
}
