package com.ldgen.weblog.model.vo;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏后的用户信息
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    // ===================== ↓↓↓ 新增博客配置字段 ↓↓↓ =====================

    /**
     * 博客名称
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
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}