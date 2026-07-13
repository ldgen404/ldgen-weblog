package com.ldgen.weblog.model.vo.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
/**
 * 获取仪表盘基础统计数据响应VO
 *
 * @author <a href="https://github.com/ld417">ld417</a>
 * @date 2023/08/07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindDashboardStatisticsRspVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long articleTotalCount;

    private Long categoryTotalCount;

    private Long tagTotalCount;

    private Long pvTotalCount;
}
