package com.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户基础信息响应结果
 */
@Data
public class BaseUserInfoResult implements Serializable {
    /**
     * 人员id
     */
    private Long userId;

    /**
     * 姓名
     */
    private String userName;

    /**
     * 身份信息
     */
    private String identity;
}
