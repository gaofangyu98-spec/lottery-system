package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动-奖品关联表 DO
 * <p>
 * 对应数据库 activity_prize 表，记录某个活动中包含哪些奖品、奖品数量、等级和抽取状态。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityPrizeDO extends BaseDO {

    /**
     * 关联的活动id
     */
    private Long activityId;
    /**
     * 关联的奖品id
     */
    private Long prizeId;
    /**
     * 奖品数量
     */
    private Long prizeAmount;
    /**
     * 奖品等级（对应 ActivityPrizeTiersEnum 枚举名）
     */
    private String prizeTiers;
    /**
     * 奖品状态（对应 ActivityPrizeStatusEnum 枚举名）
     */
    private String status;


}
