package com.example.lotterysystem.dao.dataobject;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户表 DO
 * <p>
 * 对应数据库 user 表，存储用户基本信息。
 * 手机号通过 Encrypt 包装，由 EncryptTypeHandler 自动 AES 加解密。
 * 密码使用 SHA-256 哈希存储（不可逆）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO{

    /**
     * 用户名
     */
    private String userName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号（AES 加密存储）
     */
    private Encrypt phoneNumber;

    /**
     * 密码（SHA-256 哈希）
     */
    private String password;

    /**
     * 用户身份（对应 UserIdentityEnum 枚举名）
     */
    private String identity;
}
