package com.jusin.integration;

import com.jusin.client.DartApiClient;
import com.jusin.dto.response.CorpCodeSyncResponse;
import com.jusin.service.AdminSyncService;
import com.jusin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @MockitoBean
    private DartApiClient dartApiClient;

    @MockitoBean
    private AdminSyncService adminSyncService;

    private WebTestClient client;
    private String adminToken;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userService.createAdminIfAbsent("admin@jusin.com", "admin1234");

        when(adminSyncService.syncCorpCodes())
                .thenReturn(new CorpCodeSyncResponse(3914, 3914, 0));

        // 로그인 후 토큰 발급
        String responseBody = client.post().uri("/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{\"email\":\"admin@jusin.com\",\"password\":\"admin1234\"}")
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        // accessToken 파싱
        int idx = responseBody.indexOf("\"accessToken\":\"");
        if (idx >= 0) {
            int start = idx + 15;
            int end = responseBody.indexOf("\"", start);
            adminToken = responseBody.substring(start, end);
        }
    }

    @Test
    @DisplayName("인증 없이 admin 엔드포인트 접근 → 401")
    void adminEndpointWithoutAuth() {
        client.get().uri("/api/v1/admin/sync/corp-codes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("잘못된 토큰으로 admin 엔드포인트 접근 → 401")
    void adminEndpointWithInvalidToken() {
        client.get().uri("/api/v1/admin/sync/corp-codes")
                .header("Authorization", "Bearer invalid.token.here")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("유효한 ADMIN 토큰으로 admin 엔드포인트 접근 → 200")
    void adminEndpointWithValidAdminToken() {
        client.get().uri("/api/v1/admin/sync/corp-codes")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk();
    }
}
