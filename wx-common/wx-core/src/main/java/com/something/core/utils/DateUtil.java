package com.something.core.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {
    public static boolean isOlderThanDays(Date inputDate, int days) {
        // 将 Date 转换为 LocalDate（考虑时区）
        LocalDate inputLocalDate = inputDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(days);
        return inputLocalDate.isBefore(thirtyDaysAgo);
    }
}
