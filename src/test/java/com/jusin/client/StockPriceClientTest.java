package com.jusin.client;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockPriceClientTest {

    @Test
    @DisplayName("쉼표 포함 가격 문자열 파싱 - '66,400' → 66400")
    void parsePriceText_withComma() {
        String priceText = "66,400".replace(",", "").trim();
        BigDecimal price = new BigDecimal(priceText);
        assertThat(price).isEqualByComparingTo(new BigDecimal("66400"));
    }

    @Test
    @DisplayName("쉼표 없는 가격 문자열 파싱 - '5000' → 5000")
    void parsePriceText_withoutComma() {
        String priceText = "5000".replace(",", "").trim();
        BigDecimal price = new BigDecimal(priceText);
        assertThat(price).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("유효하지 않은 가격 문자열 - 'N/A' → NumberFormatException")
    void parsePriceText_invalid() {
        String priceText = "N/A".replace(",", "").trim();
        assertThatThrownBy(() -> new BigDecimal(priceText))
                .isInstanceOf(NumberFormatException.class);
    }
}
