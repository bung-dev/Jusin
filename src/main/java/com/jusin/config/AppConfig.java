package com.jusin.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${dart.api.base-url}")
    private String dartBaseUrl;

    @Value("${dart.api.timeout.connect:10000}")
    private int connectTimeout;

    @Value("${dart.api.timeout.read:30000}")
    private int readTimeout;

    @Bean
    public WebClient dartWebClient() {
        SslContext sslContext;
        try {
            // DART API 서버(opendart.fss.or.kr)는 TLS_RSA_WITH_AES_128_GCM_SHA256만 지원함.
            // JusinApplication static 블록에서 Security.setProperty()로 RSA 계열 암호 스위트를
            // 재활성화한 후 이 컨텍스트가 생성되므로, 인증서 검증만 우회하면 충분함.
            sslContext = SslContextBuilder.forClient()
                    .protocols("TLSv1.2")
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException("DART WebClient SSL 컨텍스트 초기화 실패", e);
        }

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .secure(spec -> spec.sslContext(sslContext));

        return WebClient.builder()
                .baseUrl(dartBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
