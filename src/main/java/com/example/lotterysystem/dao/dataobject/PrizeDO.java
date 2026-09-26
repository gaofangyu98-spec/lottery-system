package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 奖品表 DO
 * <p>
 * 对应数据库 prize 表，存储奖品基本属性（名称、图片、价格、描述）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PrizeDO extends BaseDO{
    /**
     * 奖品名
     */
    private String name;

    /**
     * 图片索引（本地文件名）
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
