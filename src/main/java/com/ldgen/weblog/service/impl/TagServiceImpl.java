package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.constant.RedisCacheKeyConstants;
import com.ldgen.weblog.exception.BusinessException;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.manager.RedisCacheManager;
import com.ldgen.weblog.model.dto.tag.AddTagRequest;
import com.ldgen.weblog.model.dto.tag.TagQueryRequest;
import com.ldgen.weblog.model.vo.SelectRspVO;
import com.ldgen.weblog.model.vo.tag.FindTagListRspVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.ldgen.weblog.model.entity.Tag;
import com.ldgen.weblog.mapper.TagMapper;
import com.ldgen.weblog.service.TagService;
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
 * 文章标签表 服务层实现。
 *
 * @author ldgen
 */
@Service
@Slf4j
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private TagMapper tagMapper;

    @Resource
    private RedisCacheManager redisCacheManager;

    /**
     * 添加分类
     *
     * @param addTagRequest
     * @return
     */
    @Override
    public BaseResponse addTag(AddTagRequest addTagRequest) {
        ThrowUtils.throwIf(addTagRequest == null, ErrorCode.PARAMS_ERROR, "标签名称不能为空");
        List<String> tagNameList = addTagRequest.getTagName().stream()
                .map(String::trim)
                .collect(Collectors.toList());

        List<Tag> existTagList = tagMapper.selectByTagNames(tagNameList);

        if (!existTagList.isEmpty()) {
            List<String> existNames = existTagList.stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            throw new BusinessException(ErrorCode.CATEGORY_NAME_IS_EXISTED,
                    "标签已存在：" + existNames);
        }
        List<Tag> tags = addTagRequest.getTagName().stream()
                .map(tagName -> Tag.builder()
                        .tagName(tagName.trim())
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(0)
                        .build()
                ).collect(Collectors.toList());
        tagMapper.insertBatch(tags);
        clearTagCaches();
        return ResultUtils.success("添加成功");
    }

    /**
     * 查询条件
     *
     * @param tagQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(TagQueryRequest tagQueryRequest) {
        if (tagQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String tagName = tagQueryRequest.getTagName();
        LocalDate startDate = tagQueryRequest.getStartDate();
        LocalDate endDate = tagQueryRequest.getEndDate();
        String sortField = tagQueryRequest.getSortField();
        String sortOrder = tagQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .like("tag_name", tagName, StringUtils.isNotBlank(tagName))
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
    public BaseResponse findTagSelectList() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.TAG_SELECT,
                Duration.ofMinutes(10),
                this::loadTagSelectList
        );
    }

    /**
     * 获取前台标签列表
     *
     * @return 标签列表
     */
    @Override
    public BaseResponse<List<FindTagListRspVO>> findTagList() {
        return redisCacheManager.getOrLoad(
                RedisCacheKeyConstants.TAG_LIST,
                Duration.ofMinutes(10),
                this::loadTagList
        );
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean removed = super.removeById(id);
        if (removed) {
            clearTagCaches();
        }
        return removed;
    }

    private BaseResponse<List<SelectRspVO>> loadTagSelectList() {
        List<Tag> tagList = tagMapper.selectAll();
        List<SelectRspVO> selectRspVOS = null;
        if (!CollectionUtils.isEmpty(tagList)) {
            selectRspVOS = tagList.stream()
                    .map(tagDO -> SelectRspVO.builder()
                            .label(tagDO.getTagName())
                            .value(tagDO.getId())
                            .build()
                    ).collect(Collectors.toList());
        }
        return ResultUtils.success(selectRspVOS);
    }

    private BaseResponse<List<FindTagListRspVO>> loadTagList() {
        List<Tag> tagList = tagMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq("is_deleted", 0)
                        .orderBy("create_time", false)
        );

        if (CollectionUtils.isEmpty(tagList)) {
            return ResultUtils.success(Collections.emptyList());
        }

        List<FindTagListRspVO> rspList = tagList.stream()
                .map(tag -> FindTagListRspVO.builder()
                        .id(tag.getId())
                        .name(tag.getTagName())
                        .build())
                .collect(Collectors.toList());

        return ResultUtils.success(rspList);
    }

    private void clearTagCaches() {
        redisCacheManager.deleteKeys(
                RedisCacheKeyConstants.TAG_SELECT,
                RedisCacheKeyConstants.TAG_LIST,
                RedisCacheKeyConstants.DASHBOARD_STATISTICS
        );
    }
}
