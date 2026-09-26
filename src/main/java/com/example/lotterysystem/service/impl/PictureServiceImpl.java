package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.PictureService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片上传服务实现类
 * <p>
 * 将上传的图片保存到本地磁盘目录（由配置项 pic.local-path 指定），
 * 使用 UUID 重命名文件避免冲突，返回文件名作为访问路径。
 */
@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

    @Value("${pic.local-path}")
    private String picLocalPath;

    /**
     * 保存上传的图片到本地磁盘
     * <p>
     * 文件名使用 UUID + 原始后缀，避免文件名冲突和安全问题。
     *
     * @param multipartFile 上传的图片文件
     * @return 保存后的文件名（UUID.后缀）
     */
    @Override
    public String savePicture(MultipartFile multipartFile) {
        // 创建上传目录（如果不存在）
        File dir = new File(picLocalPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 提取文件后缀（.jpg、.png 等）
        String fileName = multipartFile.getOriginalFilename();
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 使用 UUID 生成新的文件名，避免覆盖和冲突
        fileName = UUID.randomUUID() + suffix;

        // 保存图片到磁盘
        try {
            multipartFile.transferTo(new File(picLocalPath + "/" + fileName));
        } catch (IOException e) {
            throw new ServiceException(ServiceErrorCodeConstants.PIC_UPLOAD_ERROR);
        }

        return fileName;
    }
}
