package com.example.lotterysystem.controller;

import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.controller.param.PrizeCreateParam;
import com.example.lotterysystem.controller.result.FindPrizeListResult;
import com.example.lotterysystem.service.PictureService;
import com.example.lotterysystem.service.PrizeService;
import com.example.lotterysystem.service.dto.PageListDTO;
import com.example.lotterysystem.service.dto.PrizeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;

/**
 * 奖品管理接口
 * <p>提供创建奖品（含奖品图片上传）、分页查询奖品列表能力。</p>
 */
@RestController
public class PrizeController {

    private static final Logger logger = LoggerFactory.getLogger(PrizeController.class);

    @Autowired
    private PrizeService prizeService;

    @Autowired
    private PictureService pictureService;

    /**
     * 创建奖品（multipart 表单：JSON 参数 + 奖品图片文件）
     */
    @RequestMapping("/prize/create")
    public CommonResult<Long> createPrize(@Validated @RequestPart("param") PrizeCreateParam param ,
                                                     @RequestPart("prizePic") MultipartFile prizePic) {
        logger.info("createPrize param: {}", JacksonUtil.writeValueAsString(param));
        return CommonResult.success(prizeService.createPrize(param,prizePic));
    }

    /**
     * 分页查询奖品列表
     */
    @RequestMapping("/prize/find-list")
    public CommonResult<FindPrizeListResult> findPrizeList(PageListParam param) {
        logger.info("findPrizeList param: {}", JacksonUtil.writeValueAsString(param));
        PageListDTO<PrizeDTO> pageListDTO = prizeService.findPrizeList(param);

        return CommonResult.success(convertToFindPrizeListResult(pageListDTO));
    }

    private FindPrizeListResult convertToFindPrizeListResult(PageListDTO<PrizeDTO> pageListDTO) {
        if (pageListDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.PRIZE_LIST_ERROR);
        }
        FindPrizeListResult findPrizeListResult = new FindPrizeListResult();

        findPrizeListResult.setTotal(pageListDTO.getTotal());

        findPrizeListResult.setRecords(
                pageListDTO.getRecords().stream()
                .map(prizeDTO -> {
                    FindPrizeListResult.PrizeInfo prizeInfo = new FindPrizeListResult.PrizeInfo();
                    prizeInfo.setPrizeId(prizeDTO.getPrizeId());
                    prizeInfo.setPrizeName(prizeDTO.getName());
                    prizeInfo.setDescription(prizeDTO.getDescription());
                    prizeInfo.setPrice(prizeDTO.getPrice());
                    prizeInfo.setImageUrl(prizeDTO.getImageUrl());
                    return prizeInfo;
                }).collect(Collectors.toList()));

        return findPrizeListResult;

    }

}
