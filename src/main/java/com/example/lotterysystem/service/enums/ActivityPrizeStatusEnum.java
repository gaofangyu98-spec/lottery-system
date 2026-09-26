package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动-奖品关联状态枚举
 */
@AllArgsConstructor
@Getter
public enum ActivityPrizeStatusEnum {
    /** 初始化（未被抽取） */
    INIT(1, "初始化"),

    /** 已被抽取（该奖品已抽完） */
    COMPLETED(2, "已被抽取");


    private final Integer code;

    private final String message;

    /**
     * 根据枚举名称（忽略大小写）查找对应的枚举值
     */
    public static ActivityPrizeStatusEnum forName(String name) {
        for (ActivityPrizeStatusEnum activityPrizeStatusEnum : ActivityPrizeStatusEnum.values()) {
            if (activityPrizeStatusEnum.name().equalsIgnoreCase(name)) {
                return activityPrizeStatusEnum;
            }
        }
        return null;
    }
}
