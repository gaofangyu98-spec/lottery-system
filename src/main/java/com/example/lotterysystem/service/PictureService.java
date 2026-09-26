package com.example.lotterysystem.service;


import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传服务接口
 */
public interface PictureService {

    /**
     * 保存上传的图片到本地磁盘
     *
     * @param pic 上传的图片文件
     * @return 保存后的文件名
     */
    String savePicture(MultipartFile pic);
}
