package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.annotation.AuthCheck;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.model.dto.category.AddCategoryRequest;
import com.ldgen.weblog.model.dto.category.CategoryQueryRequest;
import com.ldgen.weblog.model.dto.category.DeleteCategoryReqVO;
import com.ldgen.weblog.model.entity.User;
import com.ldgen.weblog.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.ldgen.weblog.model.entity.TCategory;
import com.ldgen.weblog.service.TCategoryService;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

/**
 * 文章分类表 控制层。
 *
 * @author ldgen
 */
@RestController
@RequestMapping("/category")
public class TCategoryController {

    @Autowired
    private TCategoryService tCategoryService;

    @Resource
    private UserService userService;

    /**
     * 添加文章分类。
     *
     * @param addCategoryRequest 添加分类请求
     * @return 分类 ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    @ApiOperationLog(description = "添加分类")
    public BaseResponse addCategory(@RequestBody @Validated AddCategoryRequest addCategoryRequest) {
        ThrowUtils.throwIf(addCategoryRequest == null, ErrorCode.PARAMS_ERROR);
        return tCategoryService.addCategory(addCategoryRequest);
    }

    /**
     * 分页查询所有文章分类表。
     *
     * @return 所有数据
     */
    @PostMapping("/pageList")
    @AuthCheck(mustRole = "admin")
    @ApiOperationLog(description = "分页查询分类")
    public BaseResponse findCategoryPageList(@RequestBody @Validated CategoryQueryRequest categoryQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(categoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = categoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 10, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = categoryQueryRequest.getPageNum();
        QueryWrapper queryWrapper = tCategoryService.getQueryWrapper(categoryQueryRequest);
        Page<TCategory> categoryPage = tCategoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(categoryPage);
    }


    /**
     * 根据主键删除文章分类表。
     *
     * @param deleteCategoryReqVO 主键
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperationLog(description = "删除分类")
    public BaseResponse removeById(@RequestBody @Validated DeleteCategoryReqVO deleteCategoryReqVO) {
        ThrowUtils.throwIf(deleteCategoryReqVO == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteCategoryReqVO.getId();
        tCategoryService.removeById(id);
        return ResultUtils.success("删除成功");
    }


    /**
     * 分类 Select 下拉列表数据获取
     *
     * @return
     */
    @PostMapping("/select/list")
    @ApiOperationLog(description = "分类 Select 下拉列表数据获取")
    public BaseResponse findCategorySelectList() {
        return tCategoryService.findCategorySelectList();
    }
}
