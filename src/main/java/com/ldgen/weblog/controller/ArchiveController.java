package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.article.FindArchiveArticlePageListReqVO;
import com.ldgen.weblog.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArchiveController {

    @Autowired
    private ArchiveService archiveService;

    @PostMapping("/archive/list")
    @ApiOperationLog(description = "获取文章归档分页数据")
    public BaseResponse findArchivePageList(@RequestBody FindArchiveArticlePageListReqVO findArchiveArticlePageListReqVO) {
        return archiveService.findArchivePageList(findArchiveArticlePageListReqVO);
    }

}
