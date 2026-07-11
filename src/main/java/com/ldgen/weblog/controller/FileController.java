package com.ldgen.weblog.controller;

import com.ldgen.weblog.annotation.ApiOperationLog;
import com.ldgen.weblog.annotation.AuthCheck;
import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.constant.UserConstant;
import com.ldgen.weblog.model.entity.User;
import com.ldgen.weblog.service.FileService;
import com.ldgen.weblog.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;
    @Resource
    private UserService userService;

    @PostMapping("/upload")
    @ApiOperationLog(description = "文件上传")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return fileService.uploadFile(file, loginUser);
    }

}
