package com.ldgen.weblog.model.vo.blogsettings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 前台博客设置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindBlogSettingsDetailRspVO implements Serializable {

    private Long id;

    /**
     * 博客名称
     */
    private String webName;

    /**
     * 博客 Logo
     */
    private String logo;

    /**
     * 作者名
     */
    private String author;

    /**
     * 作者头像
     */
    private String userAvatar;

    /**
     * 作者简介
     */
    private String userProfile;

    /**
     * GitHub 主页
     */
    private String githubHomepage;

    /**
     * Gitee 主页
     */
    private String giteeHomepage;

    /**
     * 知乎主页
     */
    private String zhihuHomepage;

    /**
     * CSDN 主页
     */
    private String csdnHomepage;

    /**
     * 站点总访问量
     */
    private Long pvTotalCount;

    private static final long serialVersionUID = 1L;
}
