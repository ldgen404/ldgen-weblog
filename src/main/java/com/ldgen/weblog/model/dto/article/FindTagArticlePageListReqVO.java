package com.ldgen.weblog.model.dto.article;

import com.ldgen.weblog.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签页查询文章分页请求
 */
@Data
public class FindTagArticlePageListReqVO extends PageRequest implements Serializable {

    /**
     * 标签 ID
     */
    private Long tagId;
}
