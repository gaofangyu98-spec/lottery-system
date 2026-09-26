package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 中奖记录响应结果
 */
@Data
public class WinningRecordResult implements Serializable {
    /**
     * 中奖者id
     */
    private Long winnerId;

    /**
     * 中奖者姓名
     */
    private String winnerName;

    /**
     * 奖品名
     */
    private String prizeName;

    /**
     * 等级
     */
    private String prizeTier;

    /**
     * 中奖时间
     */
    private Date winningTime;

}
