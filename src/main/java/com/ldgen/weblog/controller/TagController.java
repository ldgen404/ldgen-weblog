package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.model.dto.tag.AddTagRequest;
import com.ldgen.weblog.model.dto.tag.DeleteTagRequest;
import com.ldgen.weblog.model.dto.tag.TagQueryRequest;
import com.ldgen.weblog.model.entity.User;
import com.ldgen.weblog.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ldgen.weblog.model.entity.Tag;
import com.ldgen.weblog.service.TagService;
import org.springframework.web.bind.annotation.RestController;


/**
 * 文章标签表 控制层。
 *
 * @author ldgen
 */
@RestController
@RequestMapping("/tag")
@Slf4j
public class TagController {

    @Resource
    private TagService tagService;

    @Resource
    private UserService userService;

    /**
     * 保存文章标签表。
     *
     * @param addTagRequest 文章标签表
     * @return {@code true} 保存成功，{@code false} 保存失败
     */
    @PostMapping("/add")
    @ApiOperationLog(description = "添加文章标签")
    public BaseResponse addTag(@RequestBody @Validated AddTagRequest addTagRequest) {
        ThrowUtils.throwIf(addTagRequest == null, ErrorCode.PARAMS_ERROR);
        return tagService.addTag(addTagRequest);
    }

    /**
     * 根据主键删除文章标签表。
     *
     * @param deleteTagRequest 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @PostMapping("/remove")
    @ApiOperationLog(description = "删除文章标签")
    public BaseResponse remove(@RequestBody DeleteTagRequest deleteTagRequest) {
        ThrowUtils.throwIf(deleteTagRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteTagRequest.getId();
        tagService.removeById(id);
        return ResultUtils.success("删除成功");
    }

    /**
     * 查询所有文章标签表。
     *
     * @return 所有数据
     */
    @PostMapping("/findList")
    @ApiOperationLog(description = "查询所有文章标签")
    public BaseResponse findTagSelectList() {
        return tagService.findTagSelectList();
    }

    /**
     * 获取前台标签列表
     *
     * @return 标签列表
     */
    @PostMapping("/list")
    @ApiOperationLog(description = "获取前台标签列表")
    public BaseResponse findTagList() {
        return tagService.findTagList();
    }


    /**
     * 分页查询包含模糊文章标签表。
     *
     * @param tagQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/pageList")
    @ApiOperationLog(description = "分页查询文章标签表")
    public BaseResponse findTagPageList(@RequestBody @Validated TagQueryRequest tagQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(tagQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = tagQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 10, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = tagQueryRequest.getPageNum();
        QueryWrapper queryWrapper = tagService.getQueryWrapper(tagQueryRequest);
        Page<Tag> categoryPage = tagService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(categoryPage);
    }

}
