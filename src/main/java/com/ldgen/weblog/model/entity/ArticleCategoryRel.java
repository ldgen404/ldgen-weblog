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
 * 文章所属分类关联表 实体类。
 *
 * @author ldgen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("article_category_rel")
public class ArticleCategoryRel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 分类id
     */
    private Long categoryId;

}
