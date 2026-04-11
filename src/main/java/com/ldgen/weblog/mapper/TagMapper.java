package com.ldgen.weblog.mapper;

import com.ldgen.weblog.model.entity.TCategory;
import com.mybatisflex.core.BaseMapper;
import com.ldgen.weblog.model.entity.Tag;
import com.mybatisflex.core.query.QueryWrapper;

import java.util.List;

/**
 * 文章标签表 映射层。
 *
 * @author ldgen
 */
public interface TagMapper extends BaseMapper<Tag> {


    /**
     * 根据分类名称查询
     *
     * @param
     * @return
     */
    default List<Tag> selectByTagNames(List<String> tagName) {
        // 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(Tag::getTagName).in(tagName);
        // 执行查询
        return selectListByQuery(queryWrapper);
    }

}
