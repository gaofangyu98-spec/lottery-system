package com.example.lotterysystem.controller.param;

import lombok.Data;

/**
 * 登录请求参数基类
 */
@Data
public class UserLoginParam {
    /**
     * @see com.example.lotterysystem.service.enums.UserIdentityEnum#name()
     */
    private String mandatoryIdentity;
}
