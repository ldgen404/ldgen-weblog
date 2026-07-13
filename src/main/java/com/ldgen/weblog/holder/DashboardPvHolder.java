package com.ldgen.weblog.holder;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class DashboardPvHolder {

    private static final ConcurrentHashMap<LocalDate, LongAdder> ARTICLE_PV_COUNTER = new ConcurrentHashMap<>();

    private DashboardPvHolder() {
    }

    public static void increase(LocalDate date) {
        if (date == null) {
            return;
        }
        ARTICLE_PV_COUNTER.computeIfAbsent(date, key -> new LongAdder()).increment();
    }

    public static long getPendingTotalCount() {
        return ARTICLE_PV_COUNTER.values().stream()
                .mapToLong(LongAdder::sum)
                .sum();
    }

    public static Map<LocalDate, Long> snapshot() {
        Map<LocalDate, Long> snapshot = new LinkedHashMap<>();
        ARTICLE_PV_COUNTER.forEach((date, counter) -> snapshot.put(date, counter.sum()));
        return snapshot;
    }

    public static Map<LocalDate, Long> snapshotAndReset() {
        Map<LocalDate, Long> snapshot = new LinkedHashMap<>();
        ARTICLE_PV_COUNTER.forEach((date, counter) -> {
            long count = counter.sumThenReset();
            if (count > 0) {
                snapshot.put(date, count);
            }
        });
        ARTICLE_PV_COUNTER.entrySet().removeIf(entry -> entry.getValue().sum() <= 0);
        return snapshot;
    }
}
