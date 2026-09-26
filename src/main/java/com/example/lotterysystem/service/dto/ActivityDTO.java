package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import lombok.Data;


/**
 * 活动列表项 DTO
 * <p>
 * 用于活动分页列表展示，仅包含活动基本信息（不含奖品和人员详情）。
 */
@Data
public class ActivityDTO {
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
    private String description;

    /**
     * 活动状态
     */
    private ActivityStatusEnum status;

    /**
     * 当前活动是否进行中
     */
    public Boolean valid() {
        return status.equals(ActivityStatusEnum.RUNNING);
    }
}
