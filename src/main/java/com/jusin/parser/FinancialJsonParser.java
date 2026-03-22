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

    private static final Map<String, String> ACCOUNT_MAPPING = Map.of(
            "매출액", "revenue",
            "영업이익", "operatingIncome",
            "당기순이익", "netIncome",
            "자산총계", "totalAssets",
            "부채총계", "totalLiabilities",
            "자본총계", "equity",
            "유동자산", "currentAssets",
            "유동부채", "currentLiabilities"
    );

    public ParsedFinancialData parseJsonResponse(String jsonResponse, String period) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode list = root.get("list");

            ParsedFinancialData data = new ParsedFinancialData();
            data.setPeriod(period);

            if (list != null && list.isArray()) {
                for (JsonNode item : list) {
                    String accountNm = item.get("account_nm").asText();
                    String amount = item.get("thstrm_amount").asText();

                    String field = ACCOUNT_MAPPING.get(accountNm);
                    if (field != null && !amount.isEmpty() && !amount.equals("-")) {
                        BigDecimal value = new BigDecimal(amount.replace(",", ""));
                        setField(data, field, value);
                    }
                }
            }

            return data;
        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            throw new DataProcessingException("재무제표 JSON 파싱 실패");
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
