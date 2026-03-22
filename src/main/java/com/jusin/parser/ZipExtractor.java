package com.jusin.parser;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class ZipExtractor {

    public Map<String, byte[]> extractRawFiles(byte[] zipBytes) throws IOException {
        Map<String, byte[]> rawFiles = new LinkedHashMap<>();

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(zipBytes), Charset.forName("EUC-KR"))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".xml") || name.endsWith(".xbrl")) {
                    byte[] content = zis.readAllBytes();
                    rawFiles.put(name, content);
                    log.debug("ZIP에서 파일 추출 (raw): {}, size={}bytes", name, content.length);
                }
                zis.closeEntry();
            }
        }

        return rawFiles;
    }

    public Map<String, String> extractXmlFiles(byte[] zipBytes) throws IOException {
        Map<String, String> xmlFiles = new LinkedHashMap<>();

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(zipBytes), Charset.forName("EUC-KR"))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".xml") || name.endsWith(".xbrl")) {
                    byte[] content = zis.readAllBytes();
                    xmlFiles.put(name, new String(content, StandardCharsets.UTF_8));
                    log.debug("ZIP에서 XML 파일 추출: {}", name);
                }
                zis.closeEntry();
            }
        }

        return xmlFiles;
    }
}
