package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.article.FindIndexArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.PublishArticleRequest;
import com.ldgen.weblog.model.dto.article.UpdateArticleRequest;
import com.ldgen.weblog.model.entity.Article;
import com.mybatisflex.core.service.IService;
import org.springframework.transaction.annotation.Transactional;

public interface ArticleService extends IService<Article> {

    @Transactional(rollbackFor = Exception.class)
    BaseResponse publishArticle(PublishArticleRequest publishArticleRequest);

    @Transactional(rollbackFor = Exception.class)
    BaseResponse updateArticle(UpdateArticleRequest updateArticleRequest);

    BaseResponse findArticleDetail(Long articleId);

    /**
     * 获取首页文章分页数据
     * @param findIndexArticlePageListReqVO
     * @return
     */
    BaseResponse findArticlePageList(FindIndexArticlePageListReqVO findIndexArticlePageListReqVO);
}
