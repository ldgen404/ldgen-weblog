package com.ldgen.weblog.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ldgen.weblog.model.entity.TCategory;
import com.mybatisflex.core.query.QueryWrapper;

/**
 * 文章分类表 映射层。
 *
 * @author ldgen
 */
public interface TCategoryMapper extends BaseMapper<TCategory> {
    /**
     * 根据分类名称查询
     *
     * @param
     * @return
     */
    default TCategory selectByName(String categoryName) {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(TCategory::getCategoryName).eq(categoryName);
        // 执行查询
        return selectOneByQuery(queryWrapper);
    }


}
