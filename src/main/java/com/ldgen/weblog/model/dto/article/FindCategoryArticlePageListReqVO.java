package com.ldgen.weblog.model.dto.article;

import com.ldgen.weblog.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类页查询文章分页请求
 */
@Data
public class FindCategoryArticlePageListReqVO extends PageRequest implements Serializable {

    /**
     * 分类 ID
     */
    private Long categoryId;
}
