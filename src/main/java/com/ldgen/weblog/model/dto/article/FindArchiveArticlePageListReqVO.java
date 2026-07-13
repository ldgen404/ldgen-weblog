package com.ldgen.weblog.model.dto.article;

import com.ldgen.weblog.common.PageRequest;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class FindArchiveArticlePageListReqVO extends PageRequest implements Serializable {
}
