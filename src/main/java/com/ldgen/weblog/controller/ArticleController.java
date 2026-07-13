package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.annotation.AuthCheck;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.constant.UserConstant;
import com.ldgen.weblog.model.dto.article.FindCategoryArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindIndexArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.FindTagArticlePageListReqVO;
import com.ldgen.weblog.model.dto.article.PublishArticleRequest;
import com.ldgen.weblog.model.dto.article.UpdateArticleRequest;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ldgen.weblog.model.entity.Article;
import com.ldgen.weblog.service.ArticleService;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

/**
 * 文章表 控制层。
 *
 * @author ldgen
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    /**
     * 文章发布
     *
     * @param publishArticleRequest 发布文章请求类
     * @return 文章
     */
    @PostMapping("/publish")
    @ApiOperationLog(description = "文章发布")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse publishArticle(@RequestBody @Validated PublishArticleRequest publishArticleRequest) {
        return articleService.publishArticle(publishArticleRequest);
    }

    /**
     * 前台首页文章分页查询
     *
     * @param findIndexArticlePageListReqVO 请求参数
     * @return 文章分页数据
     */
    @PostMapping("/list")
    @ApiOperationLog(description = "前台首页文章分页查询")
    public BaseResponse findArticlePageList(@RequestBody FindIndexArticlePageListReqVO findIndexArticlePageListReqVO) {
        return articleService.findArticlePageList(findIndexArticlePageListReqVO);
    }

    /**
     * 前台分类页文章分页查询
     *
     * @param findCategoryArticlePageListReqVO 请求参数
     * @return 分类文章分页数据
     */
    @PostMapping("/category/list")
    @ApiOperationLog(description = "前台分类页文章分页查询")
    public BaseResponse findCategoryArticlePageList(@RequestBody FindCategoryArticlePageListReqVO findCategoryArticlePageListReqVO) {
        return articleService.findCategoryArticlePageList(findCategoryArticlePageListReqVO);
    }

    /**
     * 前台标签页文章分页查询
     *
     * @param findTagArticlePageListReqVO 请求参数
     * @return 标签文章分页数据
     */
    @PostMapping("/tag/list")
    @ApiOperationLog(description = "前台标签页文章分页查询")
    public BaseResponse findTagArticlePageList(@RequestBody FindTagArticlePageListReqVO findTagArticlePageListReqVO) {
        return articleService.findTagArticlePageList(findTagArticlePageListReqVO);
    }

    /**
     * 根据主键删除文章表。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    public boolean remove(@PathVariable BigInteger id) {
        return articleService.removeById(id);
    }


    /**
     * 更新文章表。
     * @param updateArticleRequest
     * @return
     */
    @PutMapping("update")
    @ApiOperationLog(description = "更新文章")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse update(@RequestBody @Validated UpdateArticleRequest updateArticleRequest) {
        return articleService.updateArticle(updateArticleRequest);
    }

    /**
     * 查询所有文章表。
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    public List<Article> list() {
        return articleService.list();
    }

    /**
     * 根据主键获取文章表。
     *
     * @param id 文章表主键
     * @return 文章表详情
     */
    @GetMapping("/getInfo/{id}")
    @ApiOperationLog(description = "获取文章详情")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse getInfo(@PathVariable Long id) {
        return articleService.findArticleDetail(id);
    }

    /**
     * 获取前台文章详情
     *
     * @param id 文章 ID
     * @return 前台文章详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperationLog(description = "获取前台文章详情")
    public BaseResponse detail(@PathVariable Long id) {
        return articleService.findFrontendArticleDetail(id);
    }

    /**
     * 分页查询文章表。
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    public Page<Article> page(Page<Article> page) {
        return articleService.page(page);
    }

}
