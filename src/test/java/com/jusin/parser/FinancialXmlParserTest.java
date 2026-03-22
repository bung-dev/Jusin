package com.jusin.parser;

import com.jusin.dto.response.ParsedFinancialData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialXmlParserTest {

    private final FinancialXmlParser parser = new FinancialXmlParser();

    @Test
    @DisplayName("빈 XML 파싱 시 예외 없이 불완전 데이터 반환")
    void parseEmptyXml() {
        String emptyXml = "<?xml version=\"1.0\"?><root></root>";
        ParsedFinancialData data = parser.parse(emptyXml, "2025-Q4");
        assertThat(data).isNotNull();
        assertThat(data.isIncomeStatementComplete()).isFalse();
        assertThat(data.isBalanceSheetComplete()).isFalse();
    }

    @Test
    @DisplayName("최소 XBRL XML 파싱 - 매출액 추출")
    void parseMinimalXbrl() {
        String xbrl = """
                <?xml version="1.0" encoding="UTF-8"?>
                <xbrl xmlns:ifrs-full="http://xbrl.ifrs.org/taxonomy/2021-03-24/ifrs-full"
                      xmlns:xbrli="http://www.xbrl.org/2003/instance">
                    <xbrli:context id="CI_2025">
                        <xbrli:entity><xbrli:identifier>00126380</xbrli:identifier></xbrli:entity>
                        <xbrli:period><xbrli:instant>2025-12-31</xbrli:instant></xbrli:period>
                    </xbrli:context>
                    <ifrs-full:Revenue contextRef="CI_2025" decimals="-6" unitRef="KRW">77284000000000</ifrs-full:Revenue>
                </xbrl>
                """;
        ParsedFinancialData data = parser.parse(xbrl, "2025");
        assertThat(data.getPeriod()).isEqualTo("2025");
        assertThat(data.getRevenue()).isNotNull();
    }
}
