package com.jusin.util;

import java.time.LocalDateTime;

public class PeriodParseUtil {

    public static boolean isValidPeriod(String period) {
        if (period == null) return true;
        return period.equals("1m") || period.equals("3m")
            || period.equals("6m") || period.equals("1y");
    }

    public static int parseMonths(String period) {
        if (period == null) period = "1m";
        return switch (period) {
            case "1m" -> 1;
            case "3m" -> 3;
            case "6m" -> 6;
            case "1y" -> 12;
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    public static LocalDateTime calculateStartDate(String period, LocalDateTime baseDate) {
        int months = parseMonths(period);
        return baseDate.minusMonths(months);
    }

    public static String resolveLatestPeriod() {
        java.time.YearMonth reportQuarter = java.time.YearMonth.now().minusMonths(3);
        int rq = (reportQuarter.getMonthValue() - 1) / 3 + 1;
        return reportQuarter.getYear() + "-Q" + rq;
    }
}
