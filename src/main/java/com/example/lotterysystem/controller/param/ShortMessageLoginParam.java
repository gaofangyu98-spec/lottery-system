package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信验证码登录请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShortMessageLoginParam extends UserLoginParam{
    /**
     * 登录手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String loginMobile;
    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
}
