package com.jusin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.Security;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class JusinApplication {

    static {
        // DART API(opendart.fss.or.kr)는 RSA 키 교환 방식(TLS_RSA_WITH_AES_128_GCM_SHA256)만 지원함.
        // Java 24에서 RSA 키 교환 암호 스위트가 jdk.tls.disabledAlgorithms에 추가되어 하드 비활성화됨.
        // Security.setProperty()로 Spring 기동 전에 설정을 완화 — 3DES_EDE_CBC 제거로 RSA 계열 재활성화.
        Security.setProperty("jdk.tls.disabledAlgorithms",
                "SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA, " +
                "DH keySize < 1024, EC keySize < 224, anon, NULL");
    }

    public static void main(String[] args) {
        SpringApplication.run(JusinApplication.class, args);
    }
}
