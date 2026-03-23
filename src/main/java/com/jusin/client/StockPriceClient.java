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

    public BigDecimal getCurrentPrice(String stockCode) {
        log.debug("네이버 금융 현재가 조회: stockCode={}", stockCode);
        try {
            Document doc = Jsoup.connect(NAVER_FINANCE_URL + stockCode)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .get();

            Element priceElement = doc.selectFirst("p.no_today em.no_today span.blind");
            if (priceElement == null) {
                throw new ExternalApiException(
                        "네이버 금융 현재가 파싱 실패: 요소 없음 stockCode=" + stockCode);
            }

            String priceText = priceElement.text().replace(",", "").trim();
            BigDecimal price = new BigDecimal(priceText);
            log.debug("현재가 조회 완료: stockCode={}, price={}", stockCode, price);
            return price;

        } catch (IOException e) {
            log.error("네이버 금융 HTTP 요청 실패: stockCode={}, error={}", stockCode, e.getMessage());
            throw new ExternalApiException(
                    "네이버 금융 접속 실패: stockCode=" + stockCode + " / " + e.getMessage());
        } catch (NumberFormatException e) {
            log.error("현재가 파싱 실패: stockCode={}, error={}", stockCode, e.getMessage());
            throw new ExternalApiException("현재가 숫자 변환 실패: stockCode=" + stockCode);
        }
    }
}
