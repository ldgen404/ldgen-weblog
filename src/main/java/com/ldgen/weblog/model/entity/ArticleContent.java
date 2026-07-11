package com.ldgen.weblog.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigInteger;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章内容表 实体类。
 *
 * @author ldgen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("article_content")
public class ArticleContent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章内容id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 教程正文
     */
    private String content;

}
