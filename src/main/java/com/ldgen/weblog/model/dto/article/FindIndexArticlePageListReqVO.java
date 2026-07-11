package com.ldgen.weblog.model.dto.article;

import com.ldgen.weblog.common.PageRequest;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 首页查询文章分页 VO
 */
@Data
@Builder
public class FindIndexArticlePageListReqVO extends PageRequest implements Serializable  {
}
