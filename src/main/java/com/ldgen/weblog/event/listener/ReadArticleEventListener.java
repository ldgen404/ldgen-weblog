package com.ldgen.weblog.event.listener;

import com.ldgen.weblog.constant.RedisCacheKeyConstants;
import com.ldgen.weblog.event.ReadArticleEvent;
import com.ldgen.weblog.holder.DashboardPvHolder;
import com.ldgen.weblog.manager.RedisCacheManager;
import com.ldgen.weblog.mapper.ArticleMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class ReadArticleEventListener {

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private RedisCacheManager redisCacheManager;

    @Async("threadPoolTaskExecutor")
    @EventListener
    public void handleReadArticleEvent(ReadArticleEvent event) {
        Long articleId = event.getArticleId();
        if (articleId == null) {
            return;
        }

        int rows = articleMapper.increaseReadNum(articleId);
        DashboardPvHolder.increase(LocalDate.now());
        redisCacheManager.deleteKeys(
                RedisCacheKeyConstants.BLOG_SETTINGS,
                RedisCacheKeyConstants.DASHBOARD_STATISTICS,
                RedisCacheKeyConstants.DASHBOARD_PV_WEEKLY
        );
        log.info("异步更新文章阅读量成功, articleId: {}, rows: {}", articleId, rows);
    }
}
