package com.jusin.parser;

import com.jusin.exception.DataProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CorpCodeXmlParser {

    public record CorpCodeEntry(String corpCode, String corpName, String stockCode, String modifyDate) {}

    public List<CorpCodeEntry> parse(byte[] xmlBytes) {
        List<CorpCodeEntry> entries = new ArrayList<>();

        try {
            // 진단: 실제 XML 내용 앞부분 확인
            int previewLen = Math.min(500, xmlBytes.length);
            log.debug("corpCode.xml 앞 500바이트 (EUC-KR): {}",
                    new String(xmlBytes, 0, previewLen, Charset.forName("EUC-KR")));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(xmlBytes));
            document.getDocumentElement().normalize();

            log.debug("corpCode.xml 루트 요소: {}, 자식 수: {}",
                    document.getDocumentElement().getTagName(),
                    document.getDocumentElement().getChildNodes().getLength());

            // DART CORPCODE.xml 실제 구조: <result><list><corp_code>...</corp_code>...</list></result>
            // 회사 레코드 wrapper 태그가 <corp>가 아닌 <list>임
            NodeList corpList = document.getElementsByTagName("list");
            log.debug("corpCode.xml getElementsByTagName(list)={}", corpList.getLength());

            // 만약 <list>도 없으면 <corp> 태그로 fallback (이전 버전 호환)
            if (corpList.getLength() == 0) {
                corpList = document.getElementsByTagName("corp");
                log.warn("list 요소 없음 → corp 태그로 재시도: {}", corpList.getLength());
            }

            log.debug("corpCode.xml 파싱: 전체 기업 수={}", corpList.getLength());

            for (int i = 0; i < corpList.getLength(); i++) {
                Element corp = (Element) corpList.item(i);
                String corpCode = getTextContent(corp, "corp_code");
                String corpName = getTextContent(corp, "corp_name");
                String stockCode = getTextContent(corp, "stock_code");
                String modifyDate = getTextContent(corp, "modify_date");
                entries.add(new CorpCodeEntry(corpCode, corpName, stockCode, modifyDate));
            }

        } catch (Exception e) {
            throw new DataProcessingException("corpCode.xml 파싱 실패: " + e.getMessage());
        }

        return entries;
    }

    /**
     * <list> 요소 아래 첫 번째 Element 자식의 태그명을 반환한다.
     * DART가 태그명을 변경한 경우에도 동작하도록 자동 감지한다.
     */
    private String detectCorpTag(Document document) {
        NodeList listNodes = document.getElementsByTagName("list");
        if (listNodes.getLength() == 0) {
            log.warn("corpCode.xml: <list> 요소도 없음. 루트 자식 요소들:");
            NodeList rootChildren = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < Math.min(rootChildren.getLength(), 10); i++) {
                Node child = rootChildren.item(i);
                log.warn("  자식[{}]: name={}, type={}", i, child.getNodeName(), child.getNodeType());
            }
            return null;
        }
        NodeList listChildren = listNodes.item(0).getChildNodes();
        for (int i = 0; i < listChildren.getLength(); i++) {
            Node child = listChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                log.debug("자동 감지된 corp 태그명: {}", child.getNodeName());
                return child.getNodeName();
            }
        }
        return null;
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent().trim();
    }
}
