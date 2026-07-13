package com.ldgen.weblog.mapper;

import com.ldgen.weblog.model.entity.StatisticsArticlePv;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsArticlePvMapper extends BaseMapper<StatisticsArticlePv> {

    /**
     * 查询总 PV 数
     *
     * @return 总 PV
     */
    Long selectPvTotalCount();

    /**
     * 根据日期累加 PV
     *
     * @param pvDate   统计日期
     * @param pvCount  累加值
     * @return 受影响行数
     */
    int increasePvCountByDate(LocalDate pvDate, Long pvCount);

    default List<StatisticsArticlePv> selectLatestDays(LocalDate startDate) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .ge("pv_date", startDate)
                .orderBy("pv_date", true);
        return selectListByQuery(queryWrapper);
    }
}
