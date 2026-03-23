package com.jusin.integration;

import com.jusin.client.DartApiClient;
import com.jusin.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @MockitoBean
    private DartApiClient dartApiClient;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
        userService.createAdminIfAbsent("admin@jusin.com", "admin1234");
    }

    @Test
    @DisplayName("로그인 성공 → 200 + accessToken 반환")
    void loginSuccess() {
        client.post().uri("/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{\"email\":\"admin@jusin.com\",\"password\":\"admin1234\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    org.assertj.core.api.Assertions.assertThat(body).contains("accessToken");
                    org.assertj.core.api.Assertions.assertThat(body).contains("Bearer");
                });
    }

    @Test
    @DisplayName("비밀번호 불일치 → 401")
    void loginWrongPassword() {
        client.post().uri("/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{\"email\":\"admin@jusin.com\",\"password\":\"wrongpass\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("존재하지 않는 이메일 → 401")
    void loginUnknownEmail() {
        client.post().uri("/api/v1/auth/login")
                .header("Content-Type", "application/json")
                .bodyValue("{\"email\":\"notexist@jusin.com\",\"password\":\"admin1234\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
