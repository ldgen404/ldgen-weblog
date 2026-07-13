package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.constant.RedisCacheKeyConstants;
import com.ldgen.weblog.holder.DashboardPvHolder;
import com.ldgen.weblog.manager.RedisCacheManager;
import com.ldgen.weblog.mapper.ArticleMapper;
import com.ldgen.weblog.mapper.StatisticsArticlePvMapper;
import com.ldgen.weblog.mapper.TCategoryMapper;
import com.ldgen.weblog.mapper.TagMapper;
import com.ldgen.weblog.model.entity.Article;
import com.ldgen.weblog.model.entity.StatisticsArticlePv;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPublishHotspotRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPvStatisticsRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardStatisticsRspVO;
import com.ldgen.weblog.service.DashboardService;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private TCategoryMapper categoryMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private StatisticsArticlePvMapper statisticsArticlePvMapper;
    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 获取仪表盘统计信息
     * @return
     */
    @Override
    public BaseResponse<FindDashboardStatisticsRspVO> findDashboardStatistics() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.DASHBOARD_STATISTICS,
                Duration.ofSeconds(30),
                this::loadDashboardStatistics
        );
    }

    /**
     * 获取仪表盘发布热点
     * @return
     */
    @Override
    public BaseResponse<FindDashboardPublishHotspotRspVO> findDashboardPublishHotspot() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.DASHBOARD_PUBLISH_HOTSPOT,
                Duration.ofSeconds(30),
                this::loadDashboardPublishHotspot
        );
    }

    /**
     * 获取仪表盘pv统计
     * @return
     */
    @Override
    public BaseResponse<FindDashboardPvStatisticsRspVO> findDashboardPvStatistics() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.DASHBOARD_PV_WEEKLY,
                Duration.ofSeconds(30),
                this::loadDashboardPvStatistics
        );
    }

    private BaseResponse<FindDashboardStatisticsRspVO> loadDashboardStatistics() {
        long articleTotalCount = articleMapper.selectCountByQuery(QueryWrapper.create().eq("is_deleted", 0));
        long categoryTotalCount = categoryMapper.selectCountByQuery(QueryWrapper.create().eq("is_deleted", 0));
        long tagTotalCount = tagMapper.selectCountByQuery(QueryWrapper.create().eq("is_deleted", 0));
        long persistedPvTotalCount = Objects.requireNonNullElse(statisticsArticlePvMapper.selectPvTotalCount(), 0L);
        long pvTotalCount = persistedPvTotalCount + DashboardPvHolder.getPendingTotalCount();

        FindDashboardStatisticsRspVO rspVO = FindDashboardStatisticsRspVO.builder()
                .articleTotalCount(articleTotalCount)
                .categoryTotalCount(categoryTotalCount)
                .tagTotalCount(tagTotalCount)
                .pvTotalCount(pvTotalCount)
                .build();

        return ResultUtils.success(rspVO);
    }

    private BaseResponse<FindDashboardPublishHotspotRspVO> loadDashboardPublishHotspot() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(5);

        List<Article> articleList = articleMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("is_deleted", 0)
                        .ge("create_time", startMonth.atDay(1).atStartOfDay())
                        .orderBy("create_time", true)
        );

        Map<YearMonth, Long> articleCountMap = new LinkedHashMap<>();
        for (YearMonth month = startMonth; !month.isAfter(currentMonth); month = month.plusMonths(1)) {
            articleCountMap.put(month, 0L);
        }

        for (Article article : articleList) {
            LocalDateTime createTime = article.getCreateTime();
            if (createTime == null) {
                continue;
            }
            YearMonth month = YearMonth.from(createTime);
            if (!articleCountMap.containsKey(month)) {
                continue;
            }
            articleCountMap.put(month, articleCountMap.get(month) + 1);
        }

        List<String> dateList = new ArrayList<>();
        List<Long> articlePublishCountList = new ArrayList<>();
        articleCountMap.forEach((month, count) -> {
            dateList.add(month.format(MONTH_FORMATTER));
            articlePublishCountList.add(count);
        });

        return ResultUtils.success(FindDashboardPublishHotspotRspVO.builder()
                .dateList(dateList)
                .articlePublishCountList(articlePublishCountList)
                .build());
    }

    private BaseResponse<FindDashboardPvStatisticsRspVO> loadDashboardPvStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        List<StatisticsArticlePv> statisticsList = statisticsArticlePvMapper.selectLatestDays(startDate);
        Map<LocalDate, Long> persistedPvMap = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(today); date = date.plusDays(1)) {
            persistedPvMap.put(date, 0L);
        }

        for (StatisticsArticlePv statisticsArticlePv : statisticsList) {
            LocalDate pvDate = statisticsArticlePv.getPvDate();
            if (pvDate == null || !persistedPvMap.containsKey(pvDate)) {
                continue;
            }
            persistedPvMap.put(pvDate, Objects.requireNonNullElse(statisticsArticlePv.getPvCount(), 0L));
        }

        Map<LocalDate, Long> pendingPvMap = DashboardPvHolder.snapshot();
        pendingPvMap.forEach((date, count) -> {
            if (persistedPvMap.containsKey(date)) {
                persistedPvMap.put(date, persistedPvMap.get(date) + count);
            }
        });

        List<String> dateList = new ArrayList<>();
        List<Long> pvCountList = new ArrayList<>();
        persistedPvMap.forEach((date, count) -> {
            dateList.add(date.format(DAY_FORMATTER));
            pvCountList.add(count);
        });

        return ResultUtils.success(FindDashboardPvStatisticsRspVO.builder()
                .dateList(dateList)
                .pvCountList(pvCountList)
                .build());
    }
}
