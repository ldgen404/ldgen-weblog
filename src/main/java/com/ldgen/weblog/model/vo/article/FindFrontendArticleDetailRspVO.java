package com.ldgen.weblog.model.vo.article;

import com.ldgen.weblog.model.vo.category.FindCategoryListRspVO;
import com.ldgen.weblog.model.vo.tag.FindTagListRspVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFrontendArticleDetailRspVO implements Serializable {

    private Long id;

    private String title;

    private String cover;

    private String summary;

    /**
     * Markdown 正文
     */
    private String content;

    /**
     * HTML 正文
     */
    private String contentHtml;

    private FindCategoryListRspVO category;

    private List<FindTagListRspVO> tags;

    private Long readNum;

    /**
     * 正文字数
     */
    private Integer wordCount;

    /**
     * 预计阅读时长（分钟）
     */
    private Integer readingTime;

    /**
     * 上一篇
     */
    private FindAdjacentArticleRspVO preArticle;

    /**
     * 下一篇
     */
    private FindAdjacentArticleRspVO nextArticle;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
