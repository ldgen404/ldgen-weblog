package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.convert.ArticleConvert;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.mapper.ArticleMapper;
import com.ldgen.weblog.model.dto.article.FindArchiveArticlePageListReqVO;
import com.ldgen.weblog.model.entity.Article;
import com.ldgen.weblog.model.vo.article.FindArchiveArticlePageListRspVO;
import com.ldgen.weblog.model.vo.article.FindArchiveArticleRspVO;
import com.ldgen.weblog.service.ArchiveService;
import com.ldgen.weblog.service.ArticleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.mybatisflex.core.paginate.Page;
import java.time.YearMonth;
import java.util.*;

@Service
@Slf4j
public class ArchiveServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArchiveService {

    /**
     * 根据归档查询文章列表
     * @param findArchiveArticlePageListReqVO
     * @return
     */
    @Override
    public BaseResponse findArchivePageList(FindArchiveArticlePageListReqVO findArchiveArticlePageListReqVO) {
        ThrowUtils.throwIf(findArchiveArticlePageListReqVO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        long pageNum = findArchiveArticlePageListReqVO.getPageNum();
        long pageSize = findArchiveArticlePageListReqVO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 篇文章");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("is_deleted", 0)
                .orderBy("create_time", false);

        Page<Article> articlePage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<FindArchiveArticlePageListRspVO> rspPage = new Page<>(pageNum, pageSize, articlePage.getTotalRow());

        List<Article> articleList = articlePage.getRecords();
        if (CollectionUtils.isEmpty(articleList)) {
            rspPage.setRecords(Collections.emptyList());
            return ResultUtils.success(rspPage);
        }

        List<FindArchiveArticleRspVO> archiveArticleList = articleList.stream()
                .filter(article -> Objects.nonNull(article.getCreateTime()))
                .map(ArticleConvert.INSTANCE::convertDO2ArchiveArticleVO)
                .toList();

        Map<YearMonth, List<FindArchiveArticleRspVO>> archiveMap = new LinkedHashMap<>();
        for (FindArchiveArticleRspVO archiveArticle : archiveArticleList) {
            archiveMap.computeIfAbsent(archiveArticle.getCreateMonth(), key -> new ArrayList<>())
                    .add(archiveArticle);
        }

        List<FindArchiveArticlePageListRspVO> rspList = archiveMap.entrySet().stream()
                .sorted((left, right) -> right.getKey().compareTo(left.getKey()))
                .map(entry -> FindArchiveArticlePageListRspVO.builder()
                        .month(entry.getKey())
                        .articles(entry.getValue())
                        .build())
                .toList();

        rspPage.setRecords(rspList);
        return ResultUtils.success(rspPage);
    }








}
