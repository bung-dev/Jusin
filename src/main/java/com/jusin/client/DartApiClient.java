package com.jusin.client;

import com.jusin.dto.response.DartCompanyDto;
import com.jusin.dto.response.DartDisclosureListDto;
import com.jusin.exception.ExternalApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class DartApiClient {

    private final WebClient dartWebClient;

    @Value("${dart.api.key}")
    private String apiKey;

    public DartCompanyDto getCompanyByStockCode(String stockCode) {
        log.debug("DART 기업정보 조회 요청: stockCode={}", stockCode);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/company.json")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("stock_code", stockCode)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART API 클라이언트 오류: " + stockCode)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(DartCompanyDto.class)
                .doOnNext(dto -> log.debug("DART 기업정보 응답: status={}, corp={}", dto.getStatus(), dto.getCorpName()))
                .block();
    }

    /**
     * corpCode로 기업 상세 조회 (sector, listDate 등 포함)
     */
    public DartCompanyDto getCompanyByCorpCode(String corpCode) {
        log.debug("DART 기업정보 조회 요청 (corpCode): corpCode={}", corpCode);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/company.json")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("corp_code", corpCode)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART API 클라이언트 오류: " + corpCode)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(DartCompanyDto.class)
                .doOnNext(dto -> log.debug("DART 기업정보 응답: status={}, corp={}", dto.getStatus(), dto.getCorpName()))
                .block();
    }

    public DartCompanyDto getCompanyByName(String corpName) {
        log.debug("DART 기업명 검색: name={}", corpName);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/company.json")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("corp_name", corpName)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART API 클라이언트 오류: " + corpName)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(DartCompanyDto.class)
                .block();
    }

    public DartDisclosureListDto getDisclosureList(String corpCode, String startDate, String reportType) {
        log.debug("DART 공시목록 조회: corpCode={}, startDate={}", corpCode, startDate);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/list.json")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("corp_code", corpCode)
                        .queryParam("bgn_de", startDate)
                        .queryParam("pblntf_ty", reportType)
                        .queryParam("page_count", 40)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART API 공시목록 클라이언트 오류: " + corpCode)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(DartDisclosureListDto.class)
                .block();
    }

    public byte[] getDisclosureDocument(String rceptNo) {
        log.debug("DART 공시 원문 조회: rceptNo={}", rceptNo);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/document.xml")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("rcept_no", rceptNo)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART 공시 원문 클라이언트 오류: " + rceptNo)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(byte[].class)
                .block();
    }

    public String getFinancialStatement(String corpCode, String year, String reportCode) {
        log.debug("DART 재무제표 조회: corpCode={}, year={}, report={}", corpCode, year, reportCode);

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fnlttSinglAcnt.json")
                        .queryParam("crtfc_key", apiKey)
                        .queryParam("corp_code", corpCode)
                        .queryParam("bsns_year", year)
                        .queryParam("reprt_code", reportCode)
                        .queryParam("fs_div", "CFS")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART 재무제표 클라이언트 오류: " + corpCode)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(String.class)
                .block();
    }

    public byte[] getCorpCodeZip() {
        log.debug("DART corpCode.xml ZIP 다운로드 요청");

        return dartWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/corpCode.xml")
                        .queryParam("crtfc_key", apiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ExternalApiException("DART corpCode.xml 클라이언트 오류")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ExternalApiException("DART API 서버 오류")))
                .bodyToMono(byte[].class)
                .block();
    }
}
