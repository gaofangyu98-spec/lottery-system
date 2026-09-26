package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 奖品列表响应结果（分页）
 */
@Data
public class FindPrizeListResult {
    /**
     * 总量
     */
    private Integer total;

    /**
     * 当前列表
     */
    private List<PrizeInfo> records;


    @Data
    public static class PrizeInfo implements Serializable{
        /**
         * 奖品id
         */
        private Long prizeId;

        /**
         * 名称
         */
        private String prizeName;

        /**
         * 描述
         */
        private String description;

        /**
         * 价值
         */
        private BigDecimal price;

        /**
         * 奖品图
         */
        private String imageUrl;

    }
}
