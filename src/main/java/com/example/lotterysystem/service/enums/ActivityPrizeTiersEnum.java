package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 奖品等级枚举
 */
@AllArgsConstructor
@Getter
public enum ActivityPrizeTiersEnum {

    /** 一等奖 */
    FIRST_PRIZE(1, "一等奖"),

    /** 二等奖 */
    SECOND_PRIZE(2, "二等奖"),

    /** 三等奖 */
    THIRD_PRIZE(3, "三等奖");

    private final Integer code;

    private final String message;

    /**
     * 根据枚举名称（忽略大小写）查找对应的枚举值
     */
    public static ActivityPrizeTiersEnum forName(String name) {
        for (ActivityPrizeTiersEnum activityPrizeTiersEnum : ActivityPrizeTiersEnum.values()) {
            if (activityPrizeTiersEnum.name().equalsIgnoreCase(name)) {
                return activityPrizeTiersEnum;
            }
        }
        return null;
    }

}
