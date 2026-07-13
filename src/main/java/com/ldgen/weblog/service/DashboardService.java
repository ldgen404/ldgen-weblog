package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPublishHotspotRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPvStatisticsRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardStatisticsRspVO;

public interface DashboardService {

    BaseResponse<FindDashboardStatisticsRspVO> findDashboardStatistics();

    BaseResponse<FindDashboardPublishHotspotRspVO> findDashboardPublishHotspot();

    BaseResponse<FindDashboardPvStatisticsRspVO> findDashboardPvStatistics();
}
