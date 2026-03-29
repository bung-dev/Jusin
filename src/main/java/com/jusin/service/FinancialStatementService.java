package com.jusin.service;

import com.jusin.client.DartApiClient;
import com.jusin.client.StockPriceClient;
import com.jusin.domain.entity.Company;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.dto.response.ParsedFinancialData;
import com.jusin.exception.CompanyNotFoundException;
import com.jusin.exception.InsufficientDataException;
import com.jusin.parser.FinancialJsonParser;
import com.jusin.repository.CompanyRepository;
import com.jusin.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialStatementService {

    private final FinancialStatementRepository fsRepository;
    private final CompanyRepository companyRepository;
    private final DartApiClient dartApiClient;
    private final FinancialJsonParser jsonParser;
    private final StockPriceClient stockPriceClient;

    private static final String ANNUAL_REPORT = "11011";
    private static final String Q1_REPORT     = "11013";
    private static final String HALF_REPORT   = "11012";
    private static final String Q3_REPORT     = "11014";

    @Transactional
    public List<FinancialStatement> collectAndSave(String stockCode) {
        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));

        String corpCode = company.getCompanyId();
        List<String> periods = generatePeriods();
        List<FinancialStatement> results = new ArrayList<>();

        Set<String> existingPeriods = new HashSet<>(fsRepository.findExistingPeriods(corpCode, periods));

        for (String period : periods) {
            if (existingPeriods.contains(period)) {
                log.debug("캐시 히트: corpCode={}, period={}", corpCode, period);
                continue;
            }

            try {
                ParsedFinancialData parsed = fetchFromDart(corpCode, period, stockCode);
                FinancialStatement saved = saveFinancialStatement(corpCode, period, parsed);
                results.add(saved);
                log.info("재무제표 저장 완료: corpCode={}, period={}", corpCode, period);
            } catch (Exception e) {
                log.warn("재무제표 수집 실패: corpCode={}, period={}, error={}", corpCode, period, e.getMessage());
            }
        }

        return results;
    }

    private ParsedFinancialData fetchFromDart(String corpCode, String period, String stockCode) {
        String year       = extractYear(period);
        String reportCode = resolveReportCode(period);

        String jsonResponse = dartApiClient.getFinancialStatement(corpCode, year, reportCode);
        ParsedFinancialData data = jsonParser.parseJsonResponse(jsonResponse, period);

        if (!data.isIncomeStatementComplete()) {
            log.warn("JSON 파싱 불완전: corpCode={}, period={}", corpCode, period);
        }

        if (data.getShareCount() == null && stockCode != null) {
            try {
                Long shareCount = stockPriceClient.getShareCount(stockCode);
                if (shareCount != null) {
                    data.setShareCount(shareCount);
                    log.debug("상장주식수 조회 완료: stockCode={}, shareCount={}", stockCode, shareCount);
                }
            } catch (Exception e) {
                log.warn("상장주식수 조회 실패: stockCode={}, error={}", stockCode, e.getMessage());
            }
        }

        return data;
    }

    @Transactional
    public FinancialStatement saveFinancialStatement(String companyId, String period,
                                                      ParsedFinancialData data) {
        FinancialStatement fs = FinancialStatement.builder()
                .companyId(companyId)
                .period(period)
                .statementType("COMBINED")
                .revenue(data.getRevenue())
                .operatingIncome(data.getOperatingIncome())
                .netIncome(data.getNetIncome())
                .shareCount(data.getShareCount())
                .currentAssets(data.getCurrentAssets())
                .totalAssets(data.getTotalAssets())
                .currentLiabilities(data.getCurrentLiabilities())
                .totalLiabilities(data.getTotalLiabilities())
                .equity(data.getEquity())
                .reportedDate(LocalDate.now())
                .build();

        return fsRepository.save(fs);
    }

    public List<FinancialStatement> getStatements(String stockCode) {
        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));
        return fsRepository.findByCompanyIdOrderByPeriodDesc(company.getCompanyId());
    }

    public FinancialStatement getStatement(String stockCode, String period) {
        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));
        return fsRepository.findByCompanyIdAndPeriod(company.getCompanyId(), period)
                .orElseThrow(() -> new InsufficientDataException(
                        "기간 " + period + "의 재무제표 데이터가 없습니다."));
    }

    private List<String> generatePeriods() {
        List<String> periods = new ArrayList<>();
        YearMonth now = YearMonth.now();

        for (int i = 0; i < 8; i++) {
            YearMonth target = now.minusMonths((long) i * 3);
            int year    = target.getYear();
            int quarter = (target.getMonthValue() - 1) / 3 + 1;
            periods.add(year + "-Q" + quarter);
        }

        int currentYear = now.getYear();
        for (int i = 1; i <= 3; i++) {
            periods.add((currentYear - i) + "-12");
        }

        return periods;
    }

    private String extractYear(String period) {
        return period.substring(0, 4);
    }

    private String resolveReportCode(String period) {
        if (period.endsWith("-12")) return ANNUAL_REPORT;
        if (period.endsWith("-Q1")) return Q1_REPORT;
        if (period.endsWith("-Q2")) return HALF_REPORT;
        if (period.endsWith("-Q3")) return Q3_REPORT;
        if (period.endsWith("-Q4")) return ANNUAL_REPORT;
        return ANNUAL_REPORT;
    }
}
