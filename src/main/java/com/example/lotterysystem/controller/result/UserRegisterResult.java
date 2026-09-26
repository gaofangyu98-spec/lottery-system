package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册响应结果
 */
@Data
public class UserRegisterResult implements Serializable {
    /** 新注册用户 id */
    private Long userId;
}
