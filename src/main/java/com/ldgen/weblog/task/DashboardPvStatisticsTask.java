package com.ldgen.weblog.task;

import com.ldgen.weblog.holder.DashboardPvHolder;
import com.ldgen.weblog.mapper.StatisticsArticlePvMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@Slf4j
public class DashboardPvStatisticsTask {

    @Resource
    private StatisticsArticlePvMapper statisticsArticlePvMapper;

    /**
     * 定时将内存中累计的当日 PV 落库
     */
    @Scheduled(fixedDelay = 10000)
    public void flushPendingPvStatistics() {
        Map<LocalDate, Long> pendingPvMap = DashboardPvHolder.snapshotAndReset();
        if (pendingPvMap.isEmpty()) {
            return;
        }

        pendingPvMap.forEach((date, count) -> {
            statisticsArticlePvMapper.increasePvCountByDate(date, count);
            log.info("已落库文章 PV 统计, date: {}, count: {}", date, count);
        });
    }
}
