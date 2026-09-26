package com.example.lotterysystem.service.dto;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 奖品列表项 DTO
 * <p>
 * 用于奖品分页列表展示，包含奖品基本信息。
 */
@Data
public class PrizeDTO {

    /**
     * 奖品Id
     */
    private Long prizeId;
    /**
     * 奖品名
     */
    private String name;

    /**
     * 图片索引
     */
    private String imageUrl;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 描述
     */
    private String description;

}
