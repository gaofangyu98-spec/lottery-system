package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 活动-参与人员关联状态枚举
 */
@AllArgsConstructor
@Getter
public enum ActivityUserStatusEnum {

    /** 初始化（未被抽中） */
    INIT(1, "初始化"),

    /** 已被抽取（已中奖） */
    COMPLETED(2, "已被抽取");


    private final Integer code;

    private final String message;

    /**
     * 根据枚举名称（忽略大小写）查找对应的枚举值
     */
    public static ActivityUserStatusEnum forName(String name) {
        for (ActivityUserStatusEnum activityUserStatusEnum : ActivityUserStatusEnum.values()) {
            if (activityUserStatusEnum.name().equalsIgnoreCase(name)) {
                return activityUserStatusEnum;
            }
        }
        return null;
    }


}
