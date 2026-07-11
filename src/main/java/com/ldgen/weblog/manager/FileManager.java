package com.ldgen.weblog.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.ldgen.weblog.config.CosClientConfig;
import com.ldgen.weblog.exception.BusinessException;
import com.ldgen.weblog.exception.ErrorCode;
import com.ldgen.weblog.exception.ThrowUtils;
import com.ldgen.weblog.model.dto.file.UploadFileResqest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片文件到对象存储（COS）
     * <p>
     * 该方法会对上传的图片进行校验，生成唯一的文件名，将文件临时保存到本地后上传到腾讯云对象存储，
     * 上传完成后返回可访问的URL地址，并清理临时文件。
     * </p>
     *
     * @param multipartFile    上传的文件对象，包含文件的二进制数据和元信息
     * @param uploadPathPrefix 上传路径前缀，用于指定文件在对象存储中的存放目录
     * @return UploadFileResqest 包含上传成功后的文件访问URL
     * @throws BusinessException 当文件校验失败、上传失败或系统异常时抛出业务异常
     */
    public UploadFileResqest uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        validPicture(multipartFile);

        String uuid = RandomUtil.randomString(16);
        String originFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originFilename);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);

        File tempFile = null;

        try {
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            cosManager.putPictureObject(uploadPath, tempFile);

            String url = cosClientConfig.getHost() + "/" + uploadPath;

            return UploadFileResqest.builder()
                    .url(url)
                    .build();

        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片上传失败");
        } finally {
            deleteTempFile(tempFile);
        }
    }

    /**
     * 校验上传图片的合法性
     * <p>
     * 该方法会对上传的文件进行三项校验：
     * 1. 文件不能为空
     * 2. 文件大小不能超过 2MB
     * 3. 文件格式必须为 jpeg、jpg、png 或 webp 之一
     * </p>
     */
    public void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 删除临时文件
     * <p>
     * 该方法用于清理上传过程中产生的临时文件，避免磁盘空间浪费。
     * 如果文件删除失败，会记录错误日志但不抛出异常，确保不影响主业务流程。
     * </p>
     *
     * @param file 需要删除的临时文件对象，如果为null则直接返回
     */
    public void deleteTempFile(File file) {
        if (file == null) return;
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}