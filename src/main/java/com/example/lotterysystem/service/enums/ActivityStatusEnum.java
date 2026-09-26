package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动状态枚举
 */
@AllArgsConstructor
@Getter
public enum ActivityStatusEnum {

    /** 活动进行中 */
    RUNNING(1, "活动进行中"),

    /** 活动已完成（所有奖品已抽完） */
    COMPLETED(2, "活动已完成");


    private final Integer code;

    private final String message;

    /**
     * 根据枚举名称（忽略大小写）查找对应的枚举值
     *
     * @param name 枚举名称
     * @return 匹配的枚举值，未匹配返回 null
     */
    public static ActivityStatusEnum forName(String name) {
        for (ActivityStatusEnum activityStatusEnum : ActivityStatusEnum.values()) {
            if (activityStatusEnum.name().equalsIgnoreCase(name)) {
                return activityStatusEnum;
            }
        }
        return null;
    }

}
