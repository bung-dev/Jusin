package com.jusin.controller;

import com.jusin.domain.entity.FinancialStatement;
import com.jusin.dto.response.ApiResponse;
import com.jusin.dto.response.FinancialStatementResponse;
import com.jusin.exception.InsufficientDataException;
import com.jusin.service.FinancialStatementService;
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
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Slf4j
@Validated
public class FinancialStatementController {

    private final FinancialStatementService fsService;

    /**
     * 재무제표 상세 조회
     * GET /api/v1/companies/{stockCode}/financial-statements?period=2025-Q4
     */
    @GetMapping("/{stockCode}/financial-statements")
    public ResponseEntity<ApiResponse<FinancialStatementResponse>> getFinancialStatement(
            @PathVariable @Pattern(regexp = "\\d{6}", message = "종목코드는 6자리 숫자여야 합니다.") String stockCode,
            @RequestParam(required = false) String period) {

        log.info("재무제표 조회: stockCode={}, period={}", stockCode, period);

        if (period == null) {
            List<FinancialStatement> statements = fsService.getStatements(stockCode);
            if (statements.isEmpty()) {
                throw new InsufficientDataException("재무제표 데이터가 없습니다: " + stockCode);
            }
            return ResponseEntity.ok(ApiResponse.success(
                    FinancialStatementResponse.from(statements.get(0))));
        }

        FinancialStatement fs = fsService.getStatement(stockCode, period);
        return ResponseEntity.ok(ApiResponse.success(FinancialStatementResponse.from(fs)));
    }
}
