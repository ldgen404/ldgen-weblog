package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.article.FindArchiveArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindCategoryArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindIndexArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindTagArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.PublishArticleRequest;
import com.ldgen.weblog.model.dto.article.UpdateArticleRequest;
import com.ldgen.weblog.model.entity.Article;
import com.mybatisflex.core.service.IService;
import org.springframework.transaction.annotation.Transactional;

public interface ArticleService extends IService<Article> {

    /**
     * 发布文章
     *
     * @param publishArticleRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    BaseResponse publishArticle(PublishArticleRequest publishArticleRequest);

    /**
     * 更新文章
     *
     * @param updateArticleRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    BaseResponse updateArticle(UpdateArticleRequest updateArticleRequest);

    /**
     * 获取文章详情
     *
     * @param articleId
     * @return
     */
    BaseResponse findArticleDetail(Long articleId);

    /**
     * 获取前台公开文章详情
     *
     * @param articleId 文章 ID
     * @return 前台文章详情
     */
    BaseResponse findFrontendArticleDetail(Long articleId);

    /**
     * 获取首页文章分页数据
     *
     * @param findIndexArticlePageListReqVO
     * @return
     */
    BaseResponse findArticlePageList(FindIndexArticlePageListReqVO findIndexArticlePageListReqVO);

    /**
     * 获取分类页文章分页数据
     *
     * @param findCategoryArticlePageListReqVO 请求参数
     * @return 分类文章分页数据
     */
    BaseResponse findCategoryArticlePageList(FindCategoryArticlePageListReqVO findCategoryArticlePageListReqVO);

    /**
     * 获取标签页文章分页数据
     *
     * @param findTagArticlePageListReqVO 请求参数
     * @return 标签文章分页数据
     */
    BaseResponse findTagArticlePageList(FindTagArticlePageListReqVO findTagArticlePageListReqVO);
}
