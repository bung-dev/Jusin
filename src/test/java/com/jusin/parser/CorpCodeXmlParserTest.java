package com.jusin.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorpCodeXmlParserTest {

    private final CorpCodeXmlParser parser = new CorpCodeXmlParser();

    @Test
    @DisplayName("EUC-KR 인코딩 CORPCODE.xml 파싱 - 상장사/비상장사 포함")
    void parseEucKrCorpCode() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"EUC-KR\"?>" +
                "<result>" +
                "<list>" +
                "<corp_code>00126380</corp_code>" +
                "<corp_name>삼성전자</corp_name>" +
                "<stock_code>005930</stock_code>" +
                "<modify_date>20220801</modify_date>" +
                "</list>" +
                "<list>" +
                "<corp_code>00164779</corp_code>" +
                "<corp_name>비상장회사</corp_name>" +
                "<stock_code> </stock_code>" +
                "<modify_date>20220801</modify_date>" +
                "</list>" +
                "</result>";

        byte[] bytes = xml.getBytes(Charset.forName("EUC-KR"));
        List<CorpCodeXmlParser.CorpCodeEntry> result = parser.parse(bytes);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).corpCode()).isEqualTo("00126380");
        assertThat(result.get(0).corpName()).isEqualTo("삼성전자");
        assertThat(result.get(0).stockCode()).isEqualTo("005930");
        assertThat(result.get(1).stockCode()).isEqualTo(""); // trim() 적용으로 공백→빈문자열
    }

    @Test
    @DisplayName("UTF-8 인코딩 CORPCODE.xml 파싱")
    void parseUtf8CorpCode() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<result>" +
                "<list><corp_code>00126380</corp_code><corp_name>삼성전자</corp_name>" +
                "<stock_code>005930</stock_code><modify_date>20220801</modify_date></list>" +
                "</result>";

        byte[] bytes = xml.getBytes("UTF-8");
        List<CorpCodeXmlParser.CorpCodeEntry> result = parser.parse(bytes);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).corpCode()).isEqualTo("00126380");
    }

    @Test
    @DisplayName("빈 리스트 XML 파싱 시 빈 리스트 반환")
    void parseEmptyList() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<result><status>000</status></result>";
        byte[] bytes = xml.getBytes("UTF-8");

        List<CorpCodeXmlParser.CorpCodeEntry> result = parser.parse(bytes);
        assertThat(result).isEmpty();
    }
}
