package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import lombok.Data;

import java.util.Date;


/**
 * 中奖记录 DTO
 * <p>
 * 用于展示中奖记录信息，包含中奖者、奖品名称、奖品等级和中奖时间。
 */
@Data
public class WinningRecordDTO {
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
     * 奖品等级
     */
    private ActivityPrizeTiersEnum prizeTier;

    /**
     * 中奖时间
     */
    private Date winningTime;

}
