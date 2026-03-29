package com.jusin.parser;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.jusin.dto.response.ParsedFinancialData;
import com.jusin.exception.DataProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FinancialJsonParser {

    private final ObjectMapper objectMapper;

    private static final Map<String, String> ACCOUNT_MAPPING = Map.ofEntries(
            Map.entry("매출액", "revenue"),
            Map.entry("영업수익", "revenue"),
            Map.entry("영업이익", "operatingIncome"),
            Map.entry("영업이익(손실)", "operatingIncome"),
            Map.entry("당기순이익", "netIncome"),
            Map.entry("당기순이익(손실)", "netIncome"),
            Map.entry("분기순이익", "netIncome"),
            Map.entry("분기순이익(손실)", "netIncome"),
            Map.entry("반기순이익", "netIncome"),
            Map.entry("반기순이익(손실)", "netIncome"),
            Map.entry("자산총계", "totalAssets"),
            Map.entry("부채총계", "totalLiabilities"),
            Map.entry("자본총계", "equity"),
            Map.entry("유동자산", "currentAssets"),
            Map.entry("유동부채", "currentLiabilities")
    );

    public ParsedFinancialData parseJsonResponse(String jsonResponse, String period) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode list = root.get("list");

            ParsedFinancialData data = new ParsedFinancialData();
            data.setPeriod(period);

            if (list != null && list.isArray()) {
                for (JsonNode item : list) {
                    JsonNode accountNode = item.get("account_nm");
                    JsonNode amountNode = item.get("thstrm_amount");
                    if (accountNode == null || amountNode == null) continue;
                    String accountNm = accountNode.asText();
                    String amount = amountNode.asText();

                    String field = ACCOUNT_MAPPING.get(accountNm);
                    if (field != null) {
                        BigDecimal value = parseAmount(amount);
                        if (value != null) {
                            setField(data, field, value);
                        }
                    }
                }
            }

            return data;
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            throw new DataProcessingException("재무제표 JSON 파싱 실패");
        }
    }

    private BigDecimal parseAmount(String amount) {
        if (amount == null || amount.isBlank() || amount.equals("-")) return null;
        String cleaned = amount.replace(",", "").trim();
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            cleaned = "-" + cleaned.substring(1, cleaned.length() - 1);
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("금액 파싱 실패: '{}'", amount);
            return null;
        }
    }

    private void setField(ParsedFinancialData data, String field, BigDecimal value) {
        switch (field) {
            case "revenue"            -> data.setRevenue(value);
            case "operatingIncome"    -> data.setOperatingIncome(value);
            case "netIncome"          -> data.setNetIncome(value);
            case "totalAssets"        -> data.setTotalAssets(value);
            case "totalLiabilities"   -> data.setTotalLiabilities(value);
            case "equity"             -> data.setEquity(value);
            case "currentAssets"      -> data.setCurrentAssets(value);
            case "currentLiabilities" -> data.setCurrentLiabilities(value);
            default -> log.warn("알 수 없는 필드: {}", field);
        }
    }
}
