package com.example.lotterysystem.service.dto;

import lombok.Data;

/**
 * 注册结果 DTO
 * <p>
 * 注册成功后返回新创建的用户 ID。
 */
@Data
public class UserRegisterDTO {
    /**
     * 新注册用户 ID
     */
    private Long userId;
}
