package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号密码登录请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPasswordLoginParam extends UserLoginParam{
    /**
     * 邮箱 / 手机号
     */
    @NotBlank(message = "邮箱或手机号不能为空")
    private String loginName;
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
