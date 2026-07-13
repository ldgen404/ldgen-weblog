package com.ldgen.weblog.model.vo.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindAdjacentArticleRspVO implements Serializable {

    private Long id;

    private String title;
}
