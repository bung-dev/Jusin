package com.jusin.controller;

import com.jusin.dto.response.ApiResponse;
import com.jusin.dto.response.CompanyDetailResponse;
import com.jusin.dto.response.CompanySearchResponse;
import com.jusin.service.CompanyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CompanySearchResponse>>> search(
            @RequestParam @NotBlank(message = "검색어를 입력해주세요.") String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        log.info("기업 검색 요청: q={}, limit={}", q, limit);
        List<CompanySearchResponse> results = companyService.search(q, limit);
        return ResponseEntity.ok(ApiResponse.success(results, results.size()));
    }

    @GetMapping("/{stockCode}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompany(
            @PathVariable @Pattern(regexp = "\\d{6}", message = "종목코드는 6자리 숫자여야 합니다.") String stockCode) {

        log.info("기업 상세 조회: stockCode={}", stockCode);
        CompanyDetailResponse response = companyService.getCompanyDetail(stockCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
