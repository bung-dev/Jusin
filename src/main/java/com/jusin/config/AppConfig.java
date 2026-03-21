package com.jusin.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class AppConfig {

    @Value("${dart.api.base-url}")
    private String dartBaseUrl;

    @Value("${dart.api.timeout.connect}")
    private int connectTimeout;

    @Value("${dart.api.timeout.read}")
    private int readTimeout;

    @Bean
    public WebClient dartWebClient() {
        return WebClient.builder()
                .baseUrl(dartBaseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                        .responseTimeout(Duration.ofMillis(readTimeout))
                ))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
