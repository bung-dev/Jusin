package com.jusin.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IndicatorScoreCalculatorTest {

    private final IndicatorScoreCalculator calc = new IndicatorScoreCalculator();

    @ParameterizedTest(name = "ROE {0}% → {1}점")
    @CsvSource({
        "20.0, 20",
        "15.0, 20",
        "12.0, 15",
        "7.0,  10",
        "3.0,   5",
        "-1.0,  0"
    })
    void scoreRoe(double roe, int expected) {
        assertThat(calc.scoreRoe(BigDecimal.valueOf(roe))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "부채비율 {0}% → {1}점")
    @CsvSource({
        "30.0,  15",
        "75.0,  10",
        "120.0,  5",
        "200.0,  0"
    })
    void scoreDebtRatio(double dr, int expected) {
        assertThat(calc.scoreDebtRatio(BigDecimal.valueOf(dr))).isEqualTo(expected);
    }

    @ParameterizedTest(name = "유동비율 {0} → {1}점")
    @CsvSource({
        "1.75, 10",
        "2.2,   8",
        "1.1,   5",
        "0.9,   0",
        "3.5,   5"
    })
    void scoreCurrentRatio(double cr, int expected) {
        assertThat(calc.scoreCurrentRatio(BigDecimal.valueOf(cr))).isEqualTo(expected);
    }

    @Test
    @DisplayName("PER null → 0점 (MVP: 주가 미확보)")
    void scorePer_null() { assertThat(calc.scorePer(null)).isEqualTo(0); }

    @Test
    @DisplayName("PBR null → 0점 (MVP: 주가 미확보)")
    void scorePbr_null() { assertThat(calc.scorePbr(null)).isEqualTo(0); }

    @Test
    @DisplayName("최고 점수 100점 달성 (모든 지표 최고)")
    void maxScore100() {
        int total = calc.scorePer(bd("5"))
                + calc.scoreRoe(bd("20"))
                + calc.scoreDebtRatio(bd("30"))
                + calc.scoreEpsGrowth(bd("15"))
                + calc.scorePbr(bd("0.8"))
                + calc.scoreOperatingMargin(bd("20"))
                + calc.scoreCurrentRatio(bd("1.8"));
        assertThat(total).isEqualTo(100);
    }

    @Test
    @DisplayName("MVP 기본 점수 (PER/PBR null) - 최대 70점")
    void mvpScore_noPriceData() {
        int total = calc.scorePer(null)
                + calc.scoreRoe(bd("20"))
                + calc.scoreDebtRatio(bd("30"))
                + calc.scoreEpsGrowth(bd("15"))
                + calc.scorePbr(null)
                + calc.scoreOperatingMargin(bd("20"))
                + calc.scoreCurrentRatio(bd("1.8"));
        assertThat(total).isEqualTo(70);
    }

    private BigDecimal bd(String v) { return new BigDecimal(v); }
}
