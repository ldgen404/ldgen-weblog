package com.ldgen.weblog.service.impl;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.common.ResultUtils;
import com.ldgen.weblog.exception.BusinessException;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.manager.CosManager;
import com.ldgen.weblog.manager.FileManager;
import com.ldgen.weblog.model.dto.file.UploadFileResqest;
import com.ldgen.weblog.model.entity.User;
import com.ldgen.weblog.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileManager fileManager;

    @Override
    public BaseResponse uploadFile(MultipartFile multipartFile, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 权限校验

        // 直接调用 FileManager 上传，返回 URL
        UploadFileResqest uploadFileResqest = fileManager.uploadPicture(multipartFile, "weblog");

        // 返回给前端
        return ResultUtils.success(uploadFileResqest);
    }
}
