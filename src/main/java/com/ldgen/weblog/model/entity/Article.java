package com.ldgen.weblog.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章表 实体类。
 *
 * @author ldgen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("article")
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 文章id
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章封面
     */
    private String cover;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后一次更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志位：0：未删除 1：已删除
     */
    private Integer isDeleted;

    /**
     * 被阅读次数
     */
    private Long readNum;





}
