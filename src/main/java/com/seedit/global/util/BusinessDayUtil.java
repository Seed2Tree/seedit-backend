package com.seedit.global.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public final class BusinessDayUtil {
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            // LocalDate.of(2026, 1, 1), ... KRX 휴장일
    );
    public static boolean isBusinessDay(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w != DayOfWeek.SATURDAY && w != DayOfWeek.SUNDAY && !HOLIDAYS.contains(d);
    }
    public static LocalDate plusBusinessDays(LocalDate from, int n) {
        LocalDate d = from; int added = 0;
        while (added < n) { d = d.plusDays(1); if (isBusinessDay(d)) added++; }
        return d;
    }
}