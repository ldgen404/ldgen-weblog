package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.category.AddCategoryRequest;
import com.ldgen.weblog.model.dto.category.CategoryQueryRequest;
import com.ldgen.weblog.model.vo.category.FindCategoryListRspVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ldgen.weblog.model.entity.TCategory;

import java.util.List;

/**
 * 文章分类表 服务层。
 *
 * @author ldgen
 */
public interface TCategoryService extends IService<TCategory> {

    /**
     * 添加分类
     * @param addCategoryRequest
     * @return
     */
    BaseResponse<Long> addCategory(AddCategoryRequest addCategoryRequest);


    /**
     * 构建查询条件
     * @param categoryQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(CategoryQueryRequest categoryQueryRequest);

    /**
     * 获取文章分类的 Select 列表数据
     * @return
     */
    BaseResponse findCategorySelectList();

    /**
     * 获取前台分类列表
     *
     * @return 分类列表
     */
    BaseResponse<List<FindCategoryListRspVO>> findCategoryList();
}
