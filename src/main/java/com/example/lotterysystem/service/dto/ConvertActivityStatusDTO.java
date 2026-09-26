package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 活动状态转换参数 DTO
 * <p>
 * 封装状态机流转所需的全部信息：活动 ID、奖品 ID、人员 ID 列表，
 * 以及各自的目标状态。由调用方构造后传入 ActivityStatusManager。
 */
@Data
public class ConvertActivityStatusDTO {

    /**
     * 活动id
     */
    private Long activityId;

    /**
     * 活动目标状态
     */
    private ActivityStatusEnum targetActivityStatus;

    /**
     * 奖品id
     */
    private Long prizeId;

    /**
     * 奖品目标状态
     */
    private ActivityPrizeStatusEnum targetPrizeStatus;

    /**
     * 人员id列表
     */
    private List<Long> userIds;

    /**
     * 人员目标状态
     */
    private ActivityUserStatusEnum targetUserStatus;

}
