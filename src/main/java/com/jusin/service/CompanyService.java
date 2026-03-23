package com.jusin.service;

import com.jusin.client.DartApiClient;
import com.jusin.domain.entity.Company;
import com.jusin.dto.response.CompanyDetailResponse;
import com.jusin.dto.response.CompanySearchResponse;
import com.jusin.dto.response.DartCompanyDto;
import com.jusin.exception.CompanyNotFoundException;
import com.jusin.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DartApiClient dartApiClient;

    public List<CompanySearchResponse> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어를 입력해주세요.");
        }

        if (query.matches("\\d{6}")) {
            return companyRepository.findByStockCode(query)
                    .map(c -> List.of(CompanySearchResponse.from(c)))
                    .orElseGet(() -> fetchAndSaveFromDart(query, limit));
        }

        List<Company> dbResults = companyRepository.findByCompanyNameContainingIgnoreCase(query);

        if (!dbResults.isEmpty()) {
            return dbResults.stream()
                    .limit(limit)
                    .map(CompanySearchResponse::from)
                    .toList();
        }

        return fetchAndSaveFromDart(query, limit);
    }

    public Company getOrFetchCompany(String stockCode) {
        return companyRepository.findByStockCode(stockCode)
                .orElseGet(() -> {
                    log.info("DB에 없는 기업, DART API 조회: stockCode={}", stockCode);
                    return fetchAndSave(stockCode);
                });
    }

    @Transactional
    public Company fetchAndSave(String stockCode) {
        DartCompanyDto dto = dartApiClient.getCompanyByStockCode(stockCode);

        if (dto == null || !"000".equals(dto.getStatus())) {
            throw new CompanyNotFoundException(stockCode);
        }

        return companyRepository.findByStockCode(stockCode)
                .map(existing -> {
                    existing.update(dto.getCorpName(), dto.getCeoNm(), dto.getAdres());
                    return existing;
                })
                .orElseGet(() -> {
                    Company company = Company.builder()
                            .companyId(dto.getCorpCode())
                            .companyName(dto.getCorpName())
                            .stockCode(dto.getStockCode())
                            .representative(dto.getCeoNm())
                            .address(dto.getAdres())
                            .website(dto.getHmUrl())
                            .phoneNumber(dto.getPhnNo())
                            .build();
                    return companyRepository.save(company);
                });
    }

    @Transactional
    public CompanyDetailResponse getCompanyDetail(String stockCode) {
        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));

        // sector가 없으면 DART API로 enrichment
        if (company.needsEnrichment()) {
            try {
                enrichCompanyDetail(company);
            } catch (Exception e) {
                log.warn("기업 상세 enrichment 실패 (기본 정보로 응답): stockCode={}, error={}", stockCode, e.getMessage());
            }
        }

        return CompanyDetailResponse.from(company);
    }

    @Transactional
    public void enrichCompanyDetail(Company company) {
        DartCompanyDto dto = dartApiClient.getCompanyByCorpCode(company.getCompanyId());
        if (dto == null || !"000".equals(dto.getStatus())) {
            log.warn("DART API enrichment 실패: companyId={}", company.getCompanyId());
            return;
        }

        LocalDate listDate = null;
        if (dto.getEstDt() != null && dto.getEstDt().length() == 8) {
            try {
                listDate = LocalDate.parse(dto.getEstDt(),
                        DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception e) {
                log.debug("설립일 파싱 실패: {}", dto.getEstDt());
            }
        }

        String sector = mapIndutyCode(dto.getIndutyCode());

        company.updateDetail(sector, listDate, dto.getCeoNm(),
                dto.getAdres(), dto.getHmUrl(), dto.getPhnNo());

        log.info("기업 상세 enrichment 완료: companyId={}, sector={}", company.getCompanyId(), sector);
    }

    private String mapIndutyCode(String indutyCode) {
        if (indutyCode == null) return null;
        return switch (indutyCode.substring(0, Math.min(2, indutyCode.length()))) {
            case "26" -> "반도체/전자부품";
            case "27" -> "전기/전자";
            case "30" -> "자동차";
            case "64", "65", "66" -> "금융";
            case "72" -> "소프트웨어";
            case "46" -> "도소매";
            case "35" -> "전력/가스";
            case "41", "42" -> "건설";
            case "10", "11" -> "식품";
            case "20" -> "화학";
            default -> "기타";
        };
    }

    private List<CompanySearchResponse> fetchAndSaveFromDart(String query, int limit) {
        try {
            DartCompanyDto dto = dartApiClient.getCompanyByName(query);
            if (dto != null && "000".equals(dto.getStatus())) {
                Company saved = fetchAndSave(dto.getStockCode());
                return List.of(CompanySearchResponse.from(saved));
            }
        } catch (Exception e) {
            log.warn("DART API 검색 실패: query={}, error={}", query, e.getMessage());
        }
        return List.of();
    }
}
