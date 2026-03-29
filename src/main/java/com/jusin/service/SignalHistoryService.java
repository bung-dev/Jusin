package com.jusin.service;

import com.jusin.domain.entity.Company;
import com.jusin.domain.entity.PredictionResult;
import com.jusin.domain.enums.Signal;
import com.jusin.dto.response.SignalHistoryItemDto;
import com.jusin.dto.response.SignalHistoryResponse;
import com.jusin.exception.CompanyNotFoundException;
import com.jusin.exception.InsufficientDataException;
import com.jusin.repository.CompanyRepository;
import com.jusin.repository.PredictionResultRepository;
import com.jusin.util.PeriodParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SignalHistoryService {

    private final PredictionResultRepository predictionRepository;
    private final CompanyRepository companyRepository;

    public SignalHistoryResponse getSignalHistory(String stockCode, String period) {
        if (period == null) period = "1m";
        if (!PeriodParseUtil.isValidPeriod(period)) {
            throw new IllegalArgumentException("period는 1m, 3m, 6m, 1y 중 하나여야 합니다.");
        }

        Company company = companyRepository.findByStockCode(stockCode)
                .orElseThrow(() -> new CompanyNotFoundException(stockCode));

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = PeriodParseUtil.calculateStartDate(period, endDate);

        List<PredictionResult> results = predictionRepository
                .findHistoryByCompanyIdAndDateRange(company.getCompanyId(), startDate, endDate);

        if (results.isEmpty()) {
            throw new InsufficientDataException("조회 기간 내 예측 결과가 없습니다.");
        }

        List<SignalHistoryItemDto> historyItems = buildHistoryItems(results);

        return SignalHistoryResponse.of(company.getCompanyName(), stockCode, period, historyItems);
    }

    private List<SignalHistoryItemDto> buildHistoryItems(List<PredictionResult> results) {
        List<SignalHistoryItemDto> items = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PredictionResult current = results.get(i);
            Signal previousSignal = (i + 1 < results.size()) ? results.get(i + 1).getSignal() : null;
            Boolean isChanged = previousSignal != null && !current.getSignal().equals(previousSignal);
            items.add(SignalHistoryItemDto.from(current, isChanged, isChanged ? previousSignal : null));
        }
        return items;
    }
}
