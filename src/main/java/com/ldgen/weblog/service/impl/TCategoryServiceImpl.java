package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.constant.RedisCacheKeyConstants;
import com.ldgen.weblog.exception.BusinessException;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.manager.RedisCacheManager;
import com.ldgen.weblog.mapper.TCategoryMapper;
import com.ldgen.weblog.model.dto.category.AddCategoryRequest;
import com.ldgen.weblog.model.dto.category.CategoryQueryRequest;
import com.ldgen.weblog.model.entity.TCategory;
import com.ldgen.weblog.model.vo.SelectRspVO;
import com.ldgen.weblog.model.vo.category.FindCategoryListRspVO;
import com.ldgen.weblog.service.TCategoryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 文章分类表 服务层实现。
 *
 * @author ldgen
 */
@Service
@Slf4j
public class TCategoryServiceImpl extends ServiceImpl<TCategoryMapper, TCategory> implements TCategoryService {

    @Resource
    private TCategoryMapper tCategoryMapper;

    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 、
     * 添加分类
     *
     * @param addCategoryRequest
     * @return
     */
    @Override
    public BaseResponse addCategory(AddCategoryRequest addCategoryRequest) {
        String name = addCategoryRequest.getCategoryName();
        TCategory categoryName = tCategoryMapper.selectByName(name);
        if (Objects.nonNull(categoryName)) {
            log.warn("分类名称： {}, 此分类已存在", categoryName);
            throw new BusinessException(ErrorCode.CATEGORY_NAME_IS_EXISTED);
        }
        // 构建 DO 类
        TCategory tCategory = TCategory.builder()
                .categoryName(addCategoryRequest.getCategoryName().trim())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        tCategoryMapper.insert(tCategory);
        clearCategoryCaches();
        return ResultUtils.success("添加成功");
    }


    /**
     * 查询条件
     *
     * @param categoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(CategoryQueryRequest categoryQueryRequest) {
        if (categoryQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String categoryName = categoryQueryRequest.getCategoryName();
        LocalDate startDate = categoryQueryRequest.getStartDate();
        LocalDate endDate = categoryQueryRequest.getEndDate();
        String sortField = categoryQueryRequest.getSortField();
        String sortOrder = categoryQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .like("category_name", categoryName, StringUtils.isNotBlank(categoryName))
                .ge("create_time", startDate, Objects.nonNull(startDate)) // 大于等于 startDate
                .le("update_time", endDate, Objects.nonNull(endDate))  // 小于等于 endDate
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 分类 Select 下拉列表数据获取
     *
     * @return
     */
    @Override
    public BaseResponse findCategorySelectList() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.CATEGORY_SELECT,
                Duration.ofMinutes(10),
                this::loadCategorySelectList
        );
    }

    /**
     * 获取前台分类列表
     *
     * @return 分类列表
     */
    @Override
    public BaseResponse<List<FindCategoryListRspVO>> findCategoryList() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.CATEGORY_LIST,
                Duration.ofMinutes(10),
                this::loadCategoryList
        );
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean removed = super.removeById(id);
        if (removed) {
            clearCategoryCaches();
        }
        return removed;
    }

    private BaseResponse<List<SelectRspVO>> loadCategorySelectList() {
        List<TCategory> tCategories = tCategoryMapper.selectAll();
        List<SelectRspVO> selectRspVOS = null;
        if (!CollectionUtils.isEmpty(tCategories)) {
            selectRspVOS = tCategories.stream()
                    .map(categoryDO -> SelectRspVO.builder()
                            .label(categoryDO.getCategoryName())
                            .value(categoryDO.getId())
                            .build())
                    .collect(Collectors.toList());
        }
        return ResultUtils.success(selectRspVOS);
    }

    private BaseResponse<List<FindCategoryListRspVO>> loadCategoryList() {
        List<TCategory> categoryList = tCategoryMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("is_deleted", 0)
                        .orderBy("create_time", false)
        );

        if (CollectionUtils.isEmpty(categoryList)) {
            return ResultUtils.success(Collections.emptyList());
        }

        List<FindCategoryListRspVO> rspList = categoryList.stream()
                .map(category -> FindCategoryListRspVO.builder()
                        .id(category.getId())
                        .name(category.getCategoryName())
                        .build())
                .collect(Collectors.toList());

        return ResultUtils.success(rspList);
    }

    private void clearCategoryCaches() {
        redisCacheManager.deleteKeys(
                RedisCacheKeyConstants.CATEGORY_SELECT,
                RedisCacheKeyConstants.CATEGORY_LIST,
                RedisCacheKeyConstants.DASHBOARD_STATISTICS
        );
    }

}
