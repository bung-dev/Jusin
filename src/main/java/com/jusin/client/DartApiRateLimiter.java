package com.jusin.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class DartApiRateLimiter {

    private final AtomicInteger dailyCallCount = new AtomicInteger(0);
    private static final int DAILY_LIMIT = 9000;

    public boolean canCall() {
        return dailyCallCount.get() < DAILY_LIMIT;
    }

    public void incrementCount() {
        dailyCallCount.incrementAndGet();
    }

    public int getDailyUsedCount() {
        return dailyCallCount.get();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCount() {
        dailyCallCount.set(0);
        log.info("DART API 일일 호출 카운터 초기화");
    }
}
