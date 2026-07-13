package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.annotation.AuthCheck;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.constant.UserConstant;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPublishHotspotRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardPvStatisticsRspVO;
import com.ldgen.weblog.model.vo.dashboard.FindDashboardStatisticsRspVO;
import com.ldgen.weblog.service.DashboardService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @PostMapping("/statistics")
    @ApiOperationLog(description = "获取仪表盘基础统计数据")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<FindDashboardStatisticsRspVO> findDashboardStatistics() {
        return dashboardService.findDashboardStatistics();
    }

    @PostMapping("/publish-hotspot")
    @ApiOperationLog(description = "获取近半年文章发布热点")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<FindDashboardPublishHotspotRspVO> findDashboardPublishHotspot() {
        return dashboardService.findDashboardPublishHotspot();
    }

    @PostMapping("/pv-weekly")
    @ApiOperationLog(description = "获取最近一周文章 PV 访问量")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<FindDashboardPvStatisticsRspVO> findDashboardPvStatistics() {
        return dashboardService.findDashboardPvStatistics();
    }
}
