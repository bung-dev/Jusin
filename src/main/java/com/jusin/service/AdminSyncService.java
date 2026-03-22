package com.jusin.service;

import com.jusin.client.DartApiClient;
import com.jusin.domain.entity.Company;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.exception.DataProcessingException;
import com.jusin.parser.CorpCodeXmlParser;
import com.jusin.parser.ZipExtractor;
import com.jusin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSyncService {

    private final DartApiClient dartApiClient;
    private final ZipExtractor zipExtractor;
    private final CorpCodeXmlParser corpCodeXmlParser;
    private final CompanyRepository companyRepository;

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

        // 4. 기존 데이터 upsert (companyId로 조회, 있으면 update, 없으면 신규)
        int created = 0, updated = 0;
        List<Company> toSave = new ArrayList<>();
        for (CorpCodeXmlParser.CorpCodeEntry entry : listedEntries) {
            Optional<Company> existing = companyRepository.findByCompanyId(entry.corpCode());
            if (existing.isPresent()) {
                existing.get().update(entry.corpName(), null, null);
                toSave.add(existing.get());
                updated++;
            } else {
                toSave.add(Company.builder()
                        .companyId(entry.corpCode())
                        .companyName(entry.corpName())
                        .stockCode(entry.stockCode())
                        .build());
                created++;
            }
        }
        companyRepository.saveAll(toSave);
        log.info("corpCode 동기화 완료: 신규={}, 업데이트={}, 전체 상장사={}", created, updated, listedEntries.size());
        return new CorpCodeSyncResponse(listedEntries.size(), created, updated);
    }
}
