package com.example.lotterysystem.dao.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 活动-参与人员关联表 DO
 * <p>
 * 对应数据库 activity_user 表，记录某个活动中有哪些参与人员及其中奖状态。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityUserDO extends BaseDO {

    /**
     * 关联的活动id
     */
    private Long activityId;

    /**
     * 关联的人员id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String userName;

    /**
     * 参与人员状态（对应 ActivityUserStatusEnum 枚举名）
     */
    private String status;


}
