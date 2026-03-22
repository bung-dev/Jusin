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

    public CompanyDetailResponse getCompanyDetail(String stockCode) {
        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));
        return CompanyDetailResponse.from(company);
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
