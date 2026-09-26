package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录响应结果
 */
@Data
public class UserLoginResult implements Serializable {
    /**
     * 登录JWT令牌
     */
    private String token;

    /**
     * 登录身份
     */
    private String identity;
}
