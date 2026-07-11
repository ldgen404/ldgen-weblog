package com.ldgen.weblog.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户 实体类。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 账号
     */
    @Column("userAccount")
    private String userAccount;

    /**
     * 密码
     */
    @Column("userPassword")
    private String userPassword;

    /**
     * 用户昵称
     */
    @Column("userName")
    private String userName;

    /**
     * 用户头像
     */
    @Column("userAvatar")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Column("userProfile")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Column("userRole")
    private String userRole;

    /**
     * 编辑时间
     */
    @Column("editTime")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("createTime")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("updateTime")
    private LocalDateTime updateTime;

    // ===================== ↓↓↓ 新增博客配置字段 ↓↓↓ =====================

    /**
     * 博客Logo
     */
    @Column("webName")
    private String webName;

    /**
     * 博客Logo
     */
    @Column("logo")
    private String logo;

    /**
     * 作者名
     */
    @Column("author")
    private String author;

    /**
     * GitHub 主页
     */
    @Column("github_homepage")
    private String githubHomepage;

    /**
     * CSDN 主页
     */
    @Column("csdn_homepage")
    private String csdnHomepage;

    /**
     * Gitee 主页
     */
    @Column("gitee_homepage")
    private String giteeHomepage;

    /**
     * 知乎主页
     */
    @Column("zhihu_homepage")
    private String zhihuHomepage;

    /**
     * 是否删除
     */
    @Column(value = "isDelete", isLogicDelete = true)
    private Integer isDelete;

}
