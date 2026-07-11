package com.ldgen.weblog.model.dto.user;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

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

    private static final long serialVersionUID = 1L;
}