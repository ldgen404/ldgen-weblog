package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.event.ReadArticleEvent;
import com.ldgen.weblog.exception.BusinessException;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.mapper.ArticleCategoryRelMapper;
import com.ldgen.weblog.mapper.ArticleContentMapper;
import com.ldgen.weblog.mapper.ArticleTagRelMapper;
import com.ldgen.weblog.mapper.TagMapper;
import com.ldgen.weblog.mapper.TCategoryMapper;
import com.ldgen.weblog.model.dto.article.FindCategoryArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindIndexArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindTagArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.PublishArticleRequest;
import com.ldgen.weblog.model.dto.article.UpdateArticleRequest;
import com.ldgen.weblog.model.entity.ArticleTagRel;
import com.ldgen.weblog.model.entity.ArticleCategoryRel;
import com.ldgen.weblog.model.entity.ArticleContent;
import com.ldgen.weblog.model.entity.Tag;
import com.ldgen.weblog.model.entity.TCategory;
import com.ldgen.weblog.model.vo.article.FindArticleDetailRspVO;
import com.ldgen.weblog.model.vo.article.FindAdjacentArticleRspVO;
import com.ldgen.weblog.model.vo.article.FindFrontendArticleDetailRspVO;
import com.ldgen.weblog.model.vo.article.FindIndexArticlePageListRspVO;
import com.ldgen.weblog.model.vo.category.FindCategoryListRspVO;
import com.ldgen.weblog.model.vo.tag.FindTagListRspVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ldgen.weblog.model.entity.Article;
import com.ldgen.weblog.mapper.ArticleMapper;
import com.ldgen.weblog.service.ArticleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.ldgen.weblog.utils.MarkdownUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章表 服务层实现。
 *
 * @author ldgen
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleContentMapper articleContentMapper;
    @Resource
    private ArticleCategoryRelMapper articleCategoryRelMapper;
    @Resource
    private TCategoryMapper categoryMapper;
    @Resource
    private TagMapper tagMapper;
    @Resource
    private ArticleTagRelMapper articleTagRelMapper;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 发布文章
     *
     * @param publishArticleRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse publishArticle(PublishArticleRequest publishArticleRequest) {
        // 1.将请求类 转 实体类, 并保存
        Article articleDO = Article.builder()
                .title(publishArticleRequest.getTitle())
                .cover(publishArticleRequest.getCover())
                .summary(publishArticleRequest.getSummary())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .readNum(1L)
                .build();
        articleMapper.insert(articleDO);

        Long articleId = articleDO.getId();
        log.info("发布文章，生成文章ID: {}", articleId);

        ArticleContent articleContent = ArticleContent.builder()
                .articleId(articleId)
                .content(publishArticleRequest.getContent())
                .build();
        articleContentMapper.insert(articleContent);

        Long categoryId = publishArticleRequest.getCategoryId();
        log.info("文章分类ID: {}", categoryId);

        TCategory tCategory = categoryMapper.selectOneById(categoryId);
        log.info("查询到的分类信息: {}", tCategory);

        if (Objects.isNull(tCategory)) {
            log.warn("分类不存在, categoryId: {}", categoryId);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不存在");
        }

        ArticleCategoryRel articleCategoryRel = ArticleCategoryRel.builder()
                .articleId(articleId)
                .categoryId(categoryId)
                .build();
        articleCategoryRelMapper.insert(articleCategoryRel);

        List<String> publishTags = publishArticleRequest.getTags();
        insertTags(articleId, publishTags);

        return ResultUtils.success("发布成功");
    }

    /**
     * 更新文章
     * @param updateArticleRequest
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResponse updateArticle(UpdateArticleRequest updateArticleRequest) {
        ThrowUtils.throwIf(updateArticleRequest == null || updateArticleRequest.getId() == null,
                ErrorCode.PARAMS_ERROR, "文章 ID 不能为空");

        Long articleId = updateArticleRequest.getId();
        Article existArticle = articleMapper.selectOneById(articleId);
        ThrowUtils.throwIf(Objects.isNull(existArticle), ErrorCode.NOT_FOUND_ERROR, "文章不存在");

        Article articleDO = Article.builder()
                .id(articleId)
                .title(updateArticleRequest.getTitle())
                .cover(updateArticleRequest.getCover())
                .summary(updateArticleRequest.getSummary())
                .updateTime(LocalDateTime.now())
                .readNum(Objects.requireNonNullElse(updateArticleRequest.getReadNum(), existArticle.getReadNum()))
                .build();
        articleMapper.update(articleDO);

        saveOrUpdateArticleContent(articleId, updateArticleRequest.getContent());
        saveOrUpdateArticleCategory(articleId, updateArticleRequest.getCategoryId());
        articleTagRelMapper.deleteByQuery(QueryWrapper.create().eq("article_id", articleId));
        insertTags(articleId, updateArticleRequest.getTags());

        return ResultUtils.success(true);
    }

    /**
     * 根据文章ID查询文章详情
     * @param articleId
     * @return
     */
    @Override
    public BaseResponse findArticleDetail(Long articleId) {
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR, "文章 ID 不合法");

        Article article = articleMapper.selectOneById(articleId);
        ThrowUtils.throwIf(Objects.isNull(article), ErrorCode.NOT_FOUND_ERROR, "文章不存在");

        ArticleContent articleContent = articleContentMapper.selectOneByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );
        ArticleCategoryRel articleCategoryRel = articleCategoryRelMapper.selectOneByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );
        List<ArticleTagRel> articleTagRelList = articleTagRelMapper.selectListByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );

        List<String> tagNames = Collections.emptyList();
        if (!CollectionUtils.isEmpty(articleTagRelList)) {
            List<Long> tagIds = articleTagRelList.stream()
                    .map(ArticleTagRel::getTagId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            if (!CollectionUtils.isEmpty(tagIds)) {
                Map<Long, String> tagNameMap = tagMapper.selectListByQuery(
                                QueryWrapper.create().in("id", tagIds).eq("is_deleted", 0)
                        ).stream()
                        .collect(Collectors.toMap(Tag::getId, Tag::getTagName, (left, right) -> left));

                tagNames = articleTagRelList.stream()
                        .map(ArticleTagRel::getTagId)
                        .map(tagNameMap::get)
                        .filter(StringUtils::hasText)
                        .toList();
            }
        }

        FindArticleDetailRspVO rspVO = FindArticleDetailRspVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .cover(article.getCover())
                .summary(article.getSummary())
                .content(Objects.nonNull(articleContent) ? articleContent.getContent() : "")
                .categoryId(Objects.nonNull(articleCategoryRel) ? articleCategoryRel.getCategoryId() : null)
                .tags(tagNames)
                .readNum(article.getReadNum())
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();

        return ResultUtils.success(rspVO);
    }

    /**
     * 获取前台文章详情
     *
     * @param articleId 文章 ID
     * @return 前台文章详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse findFrontendArticleDetail(Long articleId) {
        ThrowUtils.throwIf(articleId == null || articleId <= 0, ErrorCode.PARAMS_ERROR, "文章 ID 不合法");

        Article article = articleMapper.selectOneById(articleId);
        ThrowUtils.throwIf(Objects.isNull(article) || !Objects.equals(article.getIsDeleted(), 0), ErrorCode.NOT_FOUND_ERROR, "文章不存在");

        ArticleContent articleContent = articleContentMapper.selectOneByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );

        long nextReadNum = Objects.requireNonNullElse(article.getReadNum(), 0L) + 1;
        applicationEventPublisher.publishEvent(new ReadArticleEvent(this, articleId));

        String markdownContent = Objects.nonNull(articleContent) ? articleContent.getContent() : "";
        String contentHtml = MarkdownUtils.markdownToHtml(markdownContent);
        String plainText = MarkdownUtils.markdownToPlainText(markdownContent);
        int wordCount = plainText.replaceAll("\\s+", "").length();
        int readingTime = Math.max(1, (int) Math.ceil(wordCount / 300.0D));

        Map<Long, FindCategoryListRspVO> categoryMap = buildArticleCategoryMap(List.of(articleId));
        Map<Long, List<FindTagListRspVO>> tagMap = buildArticleTagMap(List.of(articleId));
        FindAdjacentArticleRspVO preArticle = findPreviousArticle(article);
        FindAdjacentArticleRspVO nextArticle = findNextArticle(article);

        FindFrontendArticleDetailRspVO rspVO = FindFrontendArticleDetailRspVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .cover(article.getCover())
                .summary(article.getSummary())
                .content(markdownContent)
                .contentHtml(contentHtml)
                .category(categoryMap.get(articleId))
                .tags(tagMap.getOrDefault(articleId, Collections.emptyList()))
                .readNum(nextReadNum)
                .wordCount(wordCount)
                .readingTime(readingTime)
                .preArticle(preArticle)
                .nextArticle(nextArticle)
                .createTime(article.getCreateTime())
                .updateTime(article.getUpdateTime())
                .build();

        return ResultUtils.success(rspVO);
    }

    private FindAdjacentArticleRspVO findPreviousArticle(Article currentArticle) {
        Page<Article> page = this.page(
                Page.of(1, 1),
                QueryWrapper.create()
                        .eq("is_deleted", 0)
                        .lt("id", currentArticle.getId())
                        .orderBy("id", false)
        );

        List<Article> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }

        Article article = records.get(0);
        return FindAdjacentArticleRspVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .build();
    }

    private FindAdjacentArticleRspVO findNextArticle(Article currentArticle) {
        Page<Article> page = this.page(
                Page.of(1, 1),
                QueryWrapper.create()
                        .eq("is_deleted", 0)
                        .gt("id", currentArticle.getId())
                        .orderBy("id", true)
        );

        List<Article> records = page.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return null;
        }

        Article article = records.get(0);
        return FindAdjacentArticleRspVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .build();
    }

    /**
     * 保存标签
     *
     * @param articleId
     * @param publishTags
     */
    private void insertTags(Long articleId, List<String> publishTags) {
        if (articleId == null || CollectionUtils.isEmpty(publishTags)) {
            return;
        }

        List<String> tagNameList = publishTags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (CollectionUtils.isEmpty(tagNameList)) {
            return;
        }

        List<Tag> existTagList = tagMapper.selectByTagNames(tagNameList);
        Map<String, Tag> existTagMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(existTagList)) {
            existTagMap.putAll(existTagList.stream()
                    .collect(Collectors.toMap(Tag::getTagName, Function.identity(), (left, right) -> left)));
        }

        List<Tag> newTags = tagNameList.stream()
                .filter(tagName -> !existTagMap.containsKey(tagName))
                .map(tagName -> Tag.builder()
                        .tagName(tagName)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(0)
                        .build())
                .toList();
        if (!CollectionUtils.isEmpty(newTags)) {
            tagMapper.insertBatch(newTags);
            newTags.forEach(tag -> existTagMap.put(tag.getTagName(), tag));
        }

        List<ArticleTagRel> articleTagRelList = tagNameList.stream()
                .map(existTagMap::get)
                .filter(Objects::nonNull)
                .map(tag -> ArticleTagRel.builder()
                        .articleId(articleId)
                        .tagId(tag.getId())
                        .build())
                .toList();
        if (!CollectionUtils.isEmpty(articleTagRelList)) {
            articleTagRelMapper.insertBatch(articleTagRelList);
        }
    }

    /**
     * 保存或更新文章内容
     * @param articleId
     * @param content
     */
    private void saveOrUpdateArticleContent(Long articleId, String content) {
        ArticleContent articleContent = articleContentMapper.selectOneByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );

        if (Objects.nonNull(articleContent)) {
            articleContent.setContent(content);
            articleContentMapper.update(articleContent);
            return;
        }

        articleContentMapper.insert(ArticleContent.builder()
                .articleId(articleId)
                .content(content)
                .build());
    }

    /**
     * 保存或更新文章分类
     * @param articleId
     * @param categoryId
     */
    private void saveOrUpdateArticleCategory(Long articleId, Long categoryId) {
        TCategory tCategory = categoryMapper.selectOneById(categoryId);
        ThrowUtils.throwIf(Objects.isNull(tCategory), ErrorCode.PARAMS_ERROR, "分类不存在");

        ArticleCategoryRel articleCategoryRel = articleCategoryRelMapper.selectOneByQuery(
                QueryWrapper.create().eq("article_id", articleId)
        );
        if (Objects.nonNull(articleCategoryRel)) {
            articleCategoryRel.setCategoryId(categoryId);
            articleCategoryRelMapper.update(articleCategoryRel);
            return;
        }

        articleCategoryRelMapper.insert(ArticleCategoryRel.builder()
                .articleId(articleId)
                .categoryId(categoryId)
                .build());
    }

    /**
     * 根据分页查询文章列表
     * @param findIndexArticlePageListReqVO
     * @return
     */
    @Override
    public BaseResponse findArticlePageList(FindIndexArticlePageListReqVO findIndexArticlePageListReqVO) {
        ThrowUtils.throwIf(findIndexArticlePageListReqVO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        long pageNum = findIndexArticlePageListReqVO.getPageNum();
        long pageSize = findIndexArticlePageListReqVO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 篇文章");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("is_deleted", 0)
                .orderBy("create_time", false);

        Page<Article> articlePage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return buildArticlePageResponse(articlePage, pageNum, pageSize);
    }

    /**
     * 获取分类页文章分页数据
     *
     * @param findCategoryArticlePageListReqVO 请求参数
     * @return 分类页文章分页数据
     */
    @Override
    public BaseResponse findCategoryArticlePageList(FindCategoryArticlePageListReqVO findCategoryArticlePageListReqVO) {
        ThrowUtils.throwIf(findCategoryArticlePageListReqVO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Long categoryId = findCategoryArticlePageListReqVO.getCategoryId();
        ThrowUtils.throwIf(Objects.isNull(categoryId), ErrorCode.PARAMS_ERROR, "分类 ID 不能为空");

        long pageNum = findCategoryArticlePageListReqVO.getPageNum();
        long pageSize = findCategoryArticlePageListReqVO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 篇文章");

        TCategory category = categoryMapper.selectOneById(categoryId);
        ThrowUtils.throwIf(Objects.isNull(category) || !Objects.equals(category.getIsDeleted(), 0), ErrorCode.PARAMS_ERROR, "分类不存在");

        List<ArticleCategoryRel> articleCategoryRelList = articleCategoryRelMapper.selectListByQuery(
                QueryWrapper.create().eq("category_id", categoryId)
        );
        if (CollectionUtils.isEmpty(articleCategoryRelList)) {
            return ResultUtils.success(new Page<>(pageNum, pageSize, 0));
        }

        List<Long> articleIds = articleCategoryRelList.stream()
                .map(ArticleCategoryRel::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return ResultUtils.success(new Page<>(pageNum, pageSize, 0));
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .in("id", articleIds)
                .eq("is_deleted", 0)
                .orderBy("create_time", false);

        Page<Article> articlePage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return buildArticlePageResponse(articlePage, pageNum, pageSize);
    }

    /**
     * 获取标签页文章分页数据
     *
     * @param findTagArticlePageListReqVO 请求参数
     * @return 标签页文章分页数据
     */
    @Override
    public BaseResponse findTagArticlePageList(FindTagArticlePageListReqVO findTagArticlePageListReqVO) {
        ThrowUtils.throwIf(findTagArticlePageListReqVO == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        Long tagId = findTagArticlePageListReqVO.getTagId();
        ThrowUtils.throwIf(Objects.isNull(tagId), ErrorCode.PARAMS_ERROR, "标签 ID 不能为空");

        long pageNum = findTagArticlePageListReqVO.getPageNum();
        long pageSize = findTagArticlePageListReqVO.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 篇文章");

        Tag tag = tagMapper.selectOneById(tagId);
        ThrowUtils.throwIf(Objects.isNull(tag) || !Objects.equals(tag.getIsDeleted(), 0), ErrorCode.PARAMS_ERROR, "标签不存在");

        List<ArticleTagRel> articleTagRelList = articleTagRelMapper.selectListByQuery(
                QueryWrapper.create().eq("tag_id", tagId)
        );
        if (CollectionUtils.isEmpty(articleTagRelList)) {
            return ResultUtils.success(new Page<>(pageNum, pageSize, 0));
        }

        List<Long> articleIds = articleTagRelList.stream()
                .map(ArticleTagRel::getArticleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return ResultUtils.success(new Page<>(pageNum, pageSize, 0));
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .in("id", articleIds)
                .eq("is_deleted", 0)
                .orderBy("create_time", false);

        Page<Article> articlePage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        return buildArticlePageResponse(articlePage, pageNum, pageSize);
    }

    private BaseResponse buildArticlePageResponse(Page<Article> articlePage, long pageNum, long pageSize) {
        Page<FindIndexArticlePageListRspVO> rspPage = new Page<>(pageNum, pageSize, articlePage.getTotalRow());
        List<Article> articleList = articlePage.getRecords();
        if (CollectionUtils.isEmpty(articleList)) {
            rspPage.setRecords(Collections.emptyList());
            return ResultUtils.success(rspPage);
        }

        List<Long> articleIds = articleList.stream()
                .map(Article::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, FindCategoryListRspVO> articleCategoryMap = buildArticleCategoryMap(articleIds);
        Map<Long, List<FindTagListRspVO>> articleTagMap = buildArticleTagMap(articleIds);

        List<FindIndexArticlePageListRspVO> rspList = articleList.stream()
                .map(article -> convertToIndexPageRsp(article, articleCategoryMap, articleTagMap))
                .toList();

        rspPage.setRecords(rspList);
        return ResultUtils.success(rspPage);
    }

    /**
     *
     * @param article
     * @param articleCategoryMap
     * @param articleTagMap
     * @return
     */
    private FindIndexArticlePageListRspVO convertToIndexPageRsp(
            Article article,
            Map<Long, FindCategoryListRspVO> articleCategoryMap,
            Map<Long, List<FindTagListRspVO>> articleTagMap
    ) {
        return FindIndexArticlePageListRspVO.builder()
                .id(article.getId())
                .cover(article.getCover())
                .title(article.getTitle())
                .createTime(article.getCreateTime())
                .summary(article.getSummary())
                .category(articleCategoryMap.get(article.getId()))
                .tags(articleTagMap.getOrDefault(article.getId(), Collections.emptyList()))
                .build();
    }

    /**
     * 根据文章ID查询文章分类
     * @param articleIds
     * @return
     */
    private Map<Long, FindCategoryListRspVO> buildArticleCategoryMap(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyMap();
        }

        List<ArticleCategoryRel> articleCategoryRelList = articleCategoryRelMapper.selectListByQuery(
                QueryWrapper.create().in("article_id", articleIds)
        );
        if (CollectionUtils.isEmpty(articleCategoryRelList)) {
            return Collections.emptyMap();
        }

        List<Long> categoryIds = articleCategoryRelList.stream()
                .map(ArticleCategoryRel::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }

        Map<Long, TCategory> categoryMap = categoryMapper.selectListByQuery(
                        QueryWrapper.create()
                                .in("id", categoryIds)
                                .eq("is_deleted", 0)
                ).stream()
                .collect(Collectors.toMap(TCategory::getId, Function.identity(), (left, right) -> left));

        return articleCategoryRelList.stream()
                .filter(rel -> categoryMap.containsKey(rel.getCategoryId()))
                .collect(Collectors.toMap(
                        ArticleCategoryRel::getArticleId,
                        rel -> {
                            TCategory category = categoryMap.get(rel.getCategoryId());
                            return FindCategoryListRspVO.builder()
                                    .id(category.getId())
                                    .name(category.getCategoryName())
                                    .build();
                        },
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    /**
     * 根据文章ID查询文章标签
     * @param articleIds
     * @return
     */
    private Map<Long, List<FindTagListRspVO>> buildArticleTagMap(List<Long> articleIds) {
        if (CollectionUtils.isEmpty(articleIds)) {
            return Collections.emptyMap();
        }

        List<ArticleTagRel> articleTagRelList = articleTagRelMapper.selectListByQuery(
                QueryWrapper.create().in("article_id", articleIds)
        );
        if (CollectionUtils.isEmpty(articleTagRelList)) {
            return Collections.emptyMap();
        }

        List<Long> tagIds = articleTagRelList.stream()
                .map(ArticleTagRel::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(tagIds)) {
            return Collections.emptyMap();
        }

        Map<Long, Tag> tagMap = tagMapper.selectListByQuery(
                        QueryWrapper.create()
                                .in("id", tagIds)
                                .eq("is_deleted", 0)
                ).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity(), (left, right) -> left));

        return articleTagRelList.stream()
                .filter(rel -> tagMap.containsKey(rel.getTagId()))
                .collect(Collectors.groupingBy(
                        ArticleTagRel::getArticleId,
                        LinkedHashMap::new,
                        Collectors.mapping(rel -> {
                            Tag tag = tagMap.get(rel.getTagId());
                            return FindTagListRspVO.builder()
                                    .id(tag.getId())
                                    .name(tag.getTagName())
                                    .build();
                        }, Collectors.toList())
                ));
    }
}
