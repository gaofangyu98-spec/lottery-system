package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;


/**
 * 查询中奖记录请求参数
 */
@Data
public class ShowWinningRecordsParam implements Serializable {

    /**
     * 活动id
     */
    @NotNull(message = "活动id不能为空！")
    private Long activityId;

    /**
     * 奖品id
     */
    private Long prizeId;
}
