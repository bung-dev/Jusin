package com.jusin.service;

import com.jusin.client.DartApiClient;
import com.jusin.client.DartApiRateLimiter;
import com.jusin.domain.entity.Company;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.dto.response.HistoricalSyncResponse;
import com.jusin.exception.DataProcessingException;
import com.jusin.parser.CorpCodeXmlParser;
import com.jusin.parser.ZipExtractor;
import com.jusin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSyncService {

    private final DartApiClient dartApiClient;
    private final ZipExtractor zipExtractor;
    private final CorpCodeXmlParser corpCodeXmlParser;
    private final CompanyRepository companyRepository;
    private final FinancialStatementService fsService;
    private final IndicatorCalculationService indicatorService;
    private final DartApiRateLimiter rateLimiter;

    @Transactional
    public CorpCodeSyncResponse syncCorpCodes() {
        // 1. ZIP 다운로드
        byte[] zipBytes = dartApiClient.getCorpCodeZip();

        // 2. ZIP 압축 해제 → XML raw bytes 추출 (EUC-KR 인코딩 보존)
        Map<String, byte[]> files;
        try {
            files = zipExtractor.extractRawFiles(zipBytes);
        } catch (Exception e) {
            throw new DataProcessingException("corpCode.xml ZIP 압축 해제 실패: " + e.getMessage());
        }
        byte[] xmlBytes = files.values().stream().findFirst()
                .orElseThrow(() -> new DataProcessingException("corpCode.xml 파일을 찾을 수 없습니다."));

        // 3. XML 파싱 → 상장사만 필터링 (stockCode가 6자리 숫자인 것)
        List<CorpCodeXmlParser.CorpCodeEntry> entries = corpCodeXmlParser.parse(xmlBytes);
        List<CorpCodeXmlParser.CorpCodeEntry> listedEntries = entries.stream()
                .filter(e -> e.stockCode() != null && e.stockCode().matches("\\d{6}"))
                .toList();

        // 4. 기존 데이터 upsert (INSERT ... ON DUPLICATE KEY UPDATE, 배치 크기 500)
        final int BATCH_SIZE = 500;
        long startTime = System.currentTimeMillis();
        int total = listedEntries.size();
        for (int i = 0; i < total; i += BATCH_SIZE) {
            List<CorpCodeXmlParser.CorpCodeEntry> batch = listedEntries.subList(i, Math.min(i + BATCH_SIZE, total));
            for (CorpCodeXmlParser.CorpCodeEntry entry : batch) {
                companyRepository.upsertCompany(entry.corpCode(), entry.corpName(), entry.stockCode());
            }
            log.debug("upsert 진행: {}/{}", Math.min(i + BATCH_SIZE, total), total);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("corpCode 동기화 완료: 전체 상장사={}, 소요시간={}ms", total, elapsed);
        return new CorpCodeSyncResponse(total, 0, total);
    }

    @Transactional
    public HistoricalSyncResponse syncHistorical(String stockCode, int quarters) {
        log.info("소급 수집 시작: stockCode={}, quarters={}", stockCode, quarters);
        try {
            List<?> saved = fsService.collectHistorical(stockCode, quarters);
            Company company = companyRepository.findByStockCode(stockCode)
                    .orElse(null);
            if (company != null) {
                indicatorService.calculateForAllPeriods(company.getCompanyId());
            }
            log.info("소급 수집 완료: stockCode={}, 저장={}건", stockCode, saved.size());
            return HistoricalSyncResponse.of(saved.size(), quarters - saved.size(), 0);
        } catch (Exception e) {
            log.error("소급 수집 실패: stockCode={}, error={}", stockCode, e.getMessage());
            return HistoricalSyncResponse.of(0, 0, 1);
        }
    }

    public HistoricalSyncResponse syncHistoricalAll(int quarters) {
        List<Company> companies = companyRepository.findAll();
        int total = companies.size();
        int success = 0, failed = 0, skipped = 0;

        log.info("전체 기업 소급 수집 시작: 전체={}개, quarters={}", total, quarters);

        for (Company company : companies) {
            if (!rateLimiter.canCall()) {
                log.warn("DART API 일일 한도 도달, 소급 수집 중단: processed={}/{}", success + failed, total);
                break;
            }
            try {
                List<?> saved = fsService.collectHistorical(company.getStockCode(), quarters);
                if (!saved.isEmpty()) {
                    indicatorService.calculateForAllPeriods(company.getCompanyId());
                    success++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                log.warn("기업 소급 수집 실패 (건너뜀): stockCode={}, error={}",
                        company.getStockCode(), e.getMessage());
                failed++;
            }
        }

        log.info("전체 소급 수집 완료: success={}, skipped={}, failed={}", success, skipped, failed);
        return HistoricalSyncResponse.ofAll(success, skipped, failed, total);
    }
}
