package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.param.PrizeCreateParam;
import com.example.lotterysystem.dao.dataobject.PrizeDO;
import com.example.lotterysystem.dao.mapper.PrizeMapper;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * 奖品管理服务实现类
 * <p>
 * 负责奖品的创建（含图片上传）和分页列表查询。
 */
@Service
@Slf4j
public class PrizeServiceImpl implements PrizeService {

    private static final Logger logger = LoggerFactory.getLogger(PrizeServiceImpl.class);

    @Autowired
    private PictureService pictureService;
    @Autowired
    private PrizeMapper prizeMapper;

    /**
     * 创建奖品：保存奖品基本信息 + 上传奖品图片
     *
     * @param param    奖品创建参数
     * @param prizePic 奖品图片文件
     * @return 新创建的奖品 ID
     */
    @Override
    public Long createPrize(PrizeCreateParam param, MultipartFile prizePic) {
        PrizeDO prizeDO = new PrizeDO();
        prizeDO.setName(param.getPrizeName());
        prizeDO.setDescription(param.getDescription());
        prizeDO.setPrice(param.getPrice());
        // 保存图片到本地磁盘，返回文件名作为 imageUrl
        String fileName = pictureService.savePicture(prizePic);
        prizeDO.setImageUrl(fileName);
        prizeMapper.insert(prizeDO);
        return prizeDO.getId();
    }

    /**
     * 奖品分页列表查询
     *
     * @param param 分页参数
     * @return 分页结果（总量 + 当前页奖品列表）
     */
    @Override
    public PageListDTO<PrizeDTO> findPrizeList(PageListParam param) {
        int total = prizeMapper.count();
        List<PrizeDTO> prizeDTOList = new ArrayList<>();
        List<PrizeDO> prizeDOList = prizeMapper.selectPrizeList(param.offset(), param.getPageSize());

        for (PrizeDO prizeDO : prizeDOList) {
            PrizeDTO prizeDTO = new PrizeDTO();
            prizeDTO.setPrizeId(prizeDO.getId());
            prizeDTO.setName(prizeDO.getName());
            prizeDTO.setDescription(prizeDO.getDescription());
            prizeDTO.setImageUrl(prizeDO.getImageUrl());
            prizeDTO.setPrice(prizeDO.getPrice());
            prizeDTOList.add(prizeDTO);
        }
        return new PageListDTO<>(total, prizeDTOList);
    }
}
