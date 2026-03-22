package com.jusin.integration;

import com.jusin.client.DartApiClient;
import com.jusin.domain.entity.Company;
import com.jusin.domain.entity.FinancialStatement;
import com.jusin.fixture.CompanyFixture;
import com.jusin.fixture.DartApiMockFixture;
import com.jusin.fixture.FinancialStatementFixture;
import com.jusin.repository.CompanyRepository;
import com.jusin.repository.FinancialIndicatorRepository;
import com.jusin.repository.FinancialStatementRepository;
import com.jusin.repository.PredictionResultRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.http.server.LocalTestWebServer;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalysisControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private FinancialStatementRepository fsRepository;

    @Autowired
    private FinancialIndicatorRepository indicatorRepository;

    @Autowired
    private PredictionResultRepository predictionRepository;

    @MockitoBean
    private DartApiClient dartApiClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        // 순서 중요: FK 의존성 고려한 역순 클리어
        predictionRepository.deleteAll();
        indicatorRepository.deleteAll();
        fsRepository.deleteAll();
        companyRepository.deleteAll();

        Company company = CompanyFixture.samsungElectronics();
        companyRepository.save(company);

        FinancialStatement fs = FinancialStatementFixture.samsung2025Q4();
        fsRepository.save(fs);

        when(dartApiClient.getCompanyByStockCode("005930"))
                .thenReturn(DartApiMockFixture.samsungCompanyDto());
        when(dartApiClient.getFinancialStatement(any(), any(), any()))
                .thenReturn(DartApiMockFixture.samsungFinancialJson());
    }

    @AfterEach
    void tearDown() {
        predictionRepository.deleteAll();
        indicatorRepository.deleteAll();
        fsRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    @DisplayName("분석 API 성공 - 신호와 스코어 포함")
    void analyzeSuccess() {
        client.get().uri("/api/v1/analysis/005930")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("\"status\":\"success\"");
                    org.assertj.core.api.Assertions.assertThat(body).contains("signal");
                    org.assertj.core.api.Assertions.assertThat(body).contains("score");
                });
    }

    @Test
    @DisplayName("기업 검색 API - 삼성전자 검색")
    void searchSuccess() {
        client.get().uri("/api/v1/companies/search?q=삼성")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("삼성전자"));
    }

    @Test
    @DisplayName("기업 상세 조회 - 005930")
    void getCompanyDetail() {
        client.get().uri("/api/v1/companies/005930")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("삼성전자");
                    org.assertj.core.api.Assertions.assertThat(body).contains("005930");
                });
    }

    @Test
    @DisplayName("잘못된 종목코드 (3자리) → 400")
    void invalidStockCode() {
        client.get().uri("/api/v1/analysis/123")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("미존재 종목코드 → 404")
    void companyNotFound() {
        when(dartApiClient.getCompanyByStockCode("999999")).thenReturn(null);
        client.get().uri("/api/v1/analysis/999999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
