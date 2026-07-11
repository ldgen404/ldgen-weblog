package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.category.AddCategoryRequest;
import com.ldgen.weblog.model.dto.tag.AddTagRequest;
import com.ldgen.weblog.model.dto.tag.TagQueryRequest;
import com.ldgen.weblog.model.vo.tag.FindTagListRspVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.ldgen.weblog.model.entity.Tag;

import java.util.List;

/**
 * 文章标签表 服务层。
 *
 * @author ldgen
 */
public interface TagService extends IService<Tag> {


    /**
     * 添加分类
     *
     * @param addTagRequest
     * @return
     */
    BaseResponse addTag(AddTagRequest addTagRequest);

    /**
     *
     * @param tagQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(TagQueryRequest tagQueryRequest);


    BaseResponse findTagSelectList();

    /**
     * 获取前台标签列表
     *
     * @return 标签列表
     */
    BaseResponse<List<FindTagListRspVO>> findTagList();
}
