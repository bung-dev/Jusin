package com.jusin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JusinApplication {
    public static void main(String[] args) {
        SpringApplication.run(JusinApplication.class, args);
    }
}
