package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动表 DO
 * <p>
 * 对应数据库 activity 表，存储活动基本信息和当前状态。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityDO extends BaseDO {

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动状态（对应 ActivityStatusEnum 枚举名）
     */
    private String status;


}
