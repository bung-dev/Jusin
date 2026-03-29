package com.jusin.client;

import com.jusin.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

@Component
@Slf4j
public class StockPriceClient {

    private static final String NAVER_FINANCE_URL =
            "https://finance.naver.com/item/main.naver?code=";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    private static final int TIMEOUT_MS = 5_000;

    public record NaverStockInfo(java.math.BigDecimal price, Long shareCount) {}

    public NaverStockInfo getStockInfo(String stockCode) {
        try {
            Document doc = fetchNaverFinancePage(stockCode);
            BigDecimal price = parsePrice(doc, stockCode);
            Long shareCount = parseShareCount(doc, stockCode);
            return new NaverStockInfo(price, shareCount);
        } catch (IOException e) {
            log.warn("네이버 금융 조회 실패: stockCode={}, error={}", stockCode, e.getMessage());
            return new NaverStockInfo(null, null);
        }
    }

    public BigDecimal getCurrentPrice(String stockCode) {
        log.debug("네이버 금융 현재가 조회: stockCode={}", stockCode);
        try {
            Document doc = fetchNaverFinancePage(stockCode);
            return parsePrice(doc, stockCode);
        } catch (IOException e) {
            log.error("네이버 금융 HTTP 요청 실패: stockCode={}, error={}", stockCode, e.getMessage());
            throw new ExternalApiException(
                    "네이버 금융 접속 실패: stockCode=" + stockCode + " / " + e.getMessage());
        }
    }

    public Long getShareCount(String stockCode) {
        log.debug("네이버 금융 상장주식수 조회: stockCode={}", stockCode);
        try {
            Document doc = fetchNaverFinancePage(stockCode);
            return parseShareCount(doc, stockCode);
        } catch (IOException e) {
            log.warn("상장주식수 HTTP 실패: stockCode={}, error={}", stockCode, e.getMessage());
            return null;
        }
    }

    private BigDecimal parsePrice(Document doc, String stockCode) {
        Element priceElement = doc.selectFirst("p.no_today em span.blind");
        if (priceElement == null) {
            throw new ExternalApiException(
                    "네이버 금융 현재가 파싱 실패: 요소 없음 stockCode=" + stockCode);
        }
        try {
            String priceText = priceElement.text().replace(",", "").trim();
            BigDecimal price = new BigDecimal(priceText);
            log.debug("현재가 조회 완료: stockCode={}, price={}", stockCode, price);
            return price;
        } catch (NumberFormatException e) {
            log.error("현재가 파싱 실패: stockCode={}, error={}", stockCode, e.getMessage());
            throw new ExternalApiException("현재가 숫자 변환 실패: stockCode=" + stockCode);
        }
    }

    private Long parseShareCount(Document doc, String stockCode) {
        // <tr><th>상장주식수</th><td><em>5,919,637,922</em></td></tr>
        Element th = doc.selectFirst("th:containsOwn(상장주식수)");
        if (th == null) return null;
        Element td = th.nextElementSibling();  // sibling td in same tr
        if (td == null) return null;
        Element em = td.selectFirst("em");
        if (em == null) return null;
        try {
            String text = em.text().replace(",", "").trim();
            Long count = Long.parseLong(text);
            log.debug("상장주식수 조회 완료: stockCode={}, shareCount={}", stockCode, count);
            return count;
        } catch (NumberFormatException e) {
            log.warn("상장주식수 파싱 실패: stockCode={}, error={}", stockCode, e.getMessage());
            return null;
        }
    }

    private Document fetchNaverFinancePage(String stockCode) throws IOException {
        return Jsoup.connect(NAVER_FINANCE_URL + stockCode)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .get();
    }
}
