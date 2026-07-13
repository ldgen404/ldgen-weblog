package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPublishHotspotRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPvStatisticsRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardStatisticsRspVO;

public interface DashboardService {

    /**
     * 获取仪表盘基础统计数据
     * @return
     */
    BaseResponse<FindDashboardStatisticsRspVO> findDashboardStatistics();

    /**
     * 获取近半年文章发布热点
     * @return
     */
    BaseResponse<FindDashboardPublishHotspotRspVO> findDashboardPublishHotspot();
    /**
     * 获取最近一周文章 PV 访问量
     * @return
     */
    BaseResponse<FindDashboardPvStatisticsRspVO> findDashboardPvStatistics();
}
