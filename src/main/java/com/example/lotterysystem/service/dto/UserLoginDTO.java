package com.example.lotterysystem.service.dto;

import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.Data;

/**
 * 登录结果 DTO
 * <p>
 * 登录成功后返回 JWT 令牌和用户身份信息。
 */
@Data
public class UserLoginDTO {
    /**
     * JWT 令牌
     */
    private String token;

    /**
     * 登录人员身份
     */
    private UserIdentityEnum identity;
}
