package com.example.lotterysystem.dao.dataobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加密数据包装类
 * <p>
 * 用于包装需要 AES 加密存储的敏感字段（如手机号）。
 * MyBatis 在写入数据库时通过 EncryptTypeHandler 自动加密，读取时自动解密。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Encrypt {
    /**
     * 明文值（由 TypeHandler 在读取时解密填充，写入时加密）
     */
    private String value;
}
