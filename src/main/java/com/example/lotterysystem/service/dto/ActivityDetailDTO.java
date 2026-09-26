package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 活动详情 DTO
 * <p>
 * 包含活动基本信息、奖品列表和参与人员列表，是活动详情接口的完整返回结构。
 * 嵌套 PrizeDTO 和 UserDTO 分别封装奖品和人员的简要信息及状态。
 */
@Data
public class ActivityDetailDTO {
    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String desc;

    /**
     * 活动状态
     */
    private ActivityStatusEnum status;

    /**
     * 当前活动是否有效（进行中）
     */
    public Boolean valid() {
        return status.equals(ActivityStatusEnum.RUNNING);
    }

    /**
     * 奖品信息列表
     */
    private List<PrizeDTO> prizeDTOList;

    /**
     * 参与人员信息列表
     */
    private List<UserDTO> userDTOList;


    /**
     * 活动关联的奖品简要信息
     */
    @Data
    public static class PrizeDTO {
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

        /**
         * 奖品等级
         */
        private ActivityPrizeTiersEnum tiers;

        /**
         * 奖品数量
         */
        private Long prizeAmount;

        /**
         * 奖品状态
         */
        private ActivityPrizeStatusEnum status;

        /**
         * 当前奖品是否可抽（未被抽取）
         */
        public Boolean valid() {
            return status != null && status.equals(ActivityPrizeStatusEnum.INIT);
        }
    }

    /**
     * 活动参与人员简要信息
     */
    @Data
    public static class UserDTO {
        /**
         * 用户id
         */
        private Long userId;
        /**
         * 姓名
         */
        private String userName;
        /**
         * 参与状态
         */
        private ActivityUserStatusEnum status;

        /**
         * 当前人员是否可参与抽奖（未被抽中）
         */
        public Boolean valid() {
            return status != null && status.equals(ActivityUserStatusEnum.INIT);
        }
    }

}
