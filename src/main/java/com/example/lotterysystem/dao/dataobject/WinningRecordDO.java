package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 中奖记录表 DO
 * <p>
 * 对应数据库 winning_record 表，记录每次抽奖的中奖结果快照（活动名、奖品名、中奖者信息等）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WinningRecordDO extends BaseDO{

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 奖品id
     */
    private Long prizeId;

    /**
     * 奖品名称
     */
    private String prizeName;

    /**
     * 奖品等级
     */
    private String prizeTier;

    /**
     * 中奖者id
     */
    private Long winnerId;

    /**
     * 中奖者姓名
     */
    private String winnerName;

    /**
     * 中奖者邮箱
     */
    private String winnerEmail;

    /**
     * 中奖者电话（AES 加密存储）
     */
    private Encrypt winnerPhoneNumber;

    /**
     * 中奖时间
     */
    private Date winningTime;

}
