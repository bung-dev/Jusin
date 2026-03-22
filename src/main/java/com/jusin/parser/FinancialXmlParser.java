package com.jusin.parser;

import com.jusin.dto.response.ParsedFinancialData;
import com.jusin.exception.DataProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class FinancialXmlParser {

    public ParsedFinancialData parse(String xmlContent, String period) {
        try {
            Document doc = parseXmlDocument(xmlContent);
            return extractFinancialData(doc, period);
        } catch (DataProcessingException e) {
            throw e;
        } catch (Exception e) {
            log.error("XML 파싱 오류: period={}, error={}", period, e.getMessage());
            throw new DataProcessingException("재무제표 XML 파싱 실패: " + e.getMessage());
        }
    }

    private Document parseXmlDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // XXE 공격 방지
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalDTD", "");
        factory.setAttribute("http://javax.xml.XMLConstants/property/accessExternalSchema", "");
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xml)));
    }

    private ParsedFinancialData extractFinancialData(Document doc, String period) {
        ParsedFinancialData data = new ParsedFinancialData();
        data.setPeriod(period);

        String contextRef = resolveContextRef(doc, period);
        log.debug("사용 contextRef: {}", contextRef);

        data.setRevenue(extractValue(doc, DartXbrlTag.REVENUE, contextRef));
        data.setOperatingIncome(extractValue(doc, DartXbrlTag.OPERATING_INCOME, contextRef));
        data.setNetIncome(extractValue(doc, DartXbrlTag.NET_INCOME, contextRef));
        data.setShareCount(extractLongValue(doc, DartXbrlTag.SHARE_COUNT, contextRef));

        data.setTotalAssets(extractValue(doc, DartXbrlTag.TOTAL_ASSETS, contextRef));
        data.setCurrentAssets(extractValue(doc, DartXbrlTag.CURRENT_ASSETS, contextRef));
        data.setTotalLiabilities(extractValue(doc, DartXbrlTag.TOTAL_LIABILITIES, contextRef));
        data.setCurrentLiabilities(extractValue(doc, DartXbrlTag.CURRENT_LIABILITIES, contextRef));
        data.setEquity(extractValue(doc, DartXbrlTag.EQUITY, contextRef));

        data.setOperatingCashFlow(extractValue(doc, DartXbrlTag.OPERATING_CASH_FLOW, contextRef));

        log.info("재무제표 파싱 완료: period={}, revenue={}", period, data.getRevenue());
        return data;
    }

    private BigDecimal extractValue(Document doc, List<String> tagCandidates, String contextRef) {
        for (String tag : tagCandidates) {
            BigDecimal value = findElementValue(doc, tag, contextRef);
            if (value != null) {
                return value;
            }
        }
        log.debug("값을 찾을 수 없음: tags={}", tagCandidates);
        return null;
    }

    private BigDecimal findElementValue(Document doc, String tagName, String contextRef) {
        String localName = tagName.contains(":") ? tagName.split(":")[1] : tagName;

        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String ctx = el.getAttribute("contextRef");
            if (ctx != null && ctx.equals(contextRef)) {
                String text = el.getTextContent().trim();
                if (!text.isEmpty()) {
                    try {
                        return new BigDecimal(text);
                    } catch (NumberFormatException e) {
                        log.warn("숫자 변환 실패: tag={}, value={}", tagName, text);
                    }
                }
            }
        }
        return null;
    }

    private Long extractLongValue(Document doc, List<String> tagCandidates, String contextRef) {
        BigDecimal value = extractValue(doc, tagCandidates, contextRef);
        return value != null ? value.longValue() : null;
    }

    private String resolveContextRef(Document doc, String period) {
        NodeList contexts = doc.getElementsByTagNameNS("*", "context");
        String yearStr = period.substring(0, 4);

        for (int i = 0; i < contexts.getLength(); i++) {
            Element ctx = (Element) contexts.item(i);
            NodeList periods = ctx.getElementsByTagNameNS("*", "instant");
            if (periods.getLength() > 0) {
                String instant = periods.item(0).getTextContent();
                if (instant.startsWith(yearStr)) {
                    return ctx.getAttribute("id");
                }
            }
        }
        return "CI_" + yearStr;
    }
}
