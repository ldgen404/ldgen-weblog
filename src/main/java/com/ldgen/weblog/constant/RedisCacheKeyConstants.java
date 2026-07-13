package com.ldgen.weblog.constant;

public final class RedisCacheKeyConstants {

    private RedisCacheKeyConstants() {
    }

    private static final String PREFIX = "weblog:";

    public static final String BLOG_SETTINGS = PREFIX + "blog:settings";
    public static final String CATEGORY_SELECT = PREFIX + "category:select";
    public static final String CATEGORY_LIST = PREFIX + "category:list";
    public static final String TAG_SELECT = PREFIX + "tag:select";
    public static final String TAG_LIST = PREFIX + "tag:list";

    public static final String ARTICLE_INDEX_PAGE_PREFIX = PREFIX + "article:index:";
    public static final String ARTICLE_CATEGORY_PAGE_PREFIX = PREFIX + "article:category:";
    public static final String ARTICLE_TAG_PAGE_PREFIX = PREFIX + "article:tag:";
    public static final String ARCHIVE_PAGE_PREFIX = PREFIX + "archive:";

    public static final String DASHBOARD_STATISTICS = PREFIX + "dashboard:statistics";
    public static final String DASHBOARD_PUBLISH_HOTSPOT = PREFIX + "dashboard:publishHotspot";
    public static final String DASHBOARD_PV_WEEKLY = PREFIX + "dashboard:pvWeekly";
}
