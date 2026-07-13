package com.ldgen.weblog.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ldgen.weblog.model.entity.Article;

/**
 * 文章表 映射层。
 *
 * @author ldgen
 */
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 阅读量 +1
     *
     * @param articleId 文章 ID
     * @return 受影响行数
     */
    int increaseReadNum(Long articleId);

}
