package com.ldgen.weblog.service;

import com.ldgen.weblog.common.BaseResponse;
import com.ldgen.weblog.model.dto.file.UploadFileResqest;
import com.ldgen.weblog.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    BaseResponse uploadFile(MultipartFile file, User loginUser);
}
