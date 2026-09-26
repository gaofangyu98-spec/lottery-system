package com.example.lotterysystem.service.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户身份枚举
 */
@AllArgsConstructor
@Getter
public enum UserIdentityEnum {

    /** 普通用户 */
    NORMAL("普通用户"),
    /** 管理员 */
    ADMIN("管理员");

    private final String identity;

    /**
     * 根据枚举名称（忽略大小写）查找对应的枚举值
     */
    public static UserIdentityEnum fromName(String name) {
        for (UserIdentityEnum userIdentityEnum : UserIdentityEnum.values()) {
            if (userIdentityEnum.name().equalsIgnoreCase(name)) {
                return userIdentityEnum;
            }
        }
        return null;
    }
}
