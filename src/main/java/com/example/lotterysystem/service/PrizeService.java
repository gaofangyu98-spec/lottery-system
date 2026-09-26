package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.param.PrizeCreateParam;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 奖品管理服务接口
 * <p>
 * 定义奖品的创建和分页列表查询操作。
 */
public interface PrizeService {

    /**
     * 创建奖品（含图片上传）
     *
     * @param param    奖品创建参数
     * @param prizePic 奖品图片文件
     * @return 新创建的奖品 ID
     */
    Long createPrize(PrizeCreateParam param, MultipartFile prizePic);

    /**
     * 奖品分页列表查询
     *
     * @param param 分页参数
     * @return 分页结果
     */
    PageListDTO<PrizeDTO> findPrizeList(PageListParam param);
}
