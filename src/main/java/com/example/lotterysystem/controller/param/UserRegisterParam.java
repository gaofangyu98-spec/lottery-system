package com.example.lotterysystem.controller.param;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterParam {

    @NotBlank(message = "用户名不能为空")
    @JsonProperty("name")
    private String userName;

    @NotBlank(message = "邮箱不能为空")
    @JsonProperty("mail")
    private String email;

    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;

    /**
     * 密码
     * 普通用户不需要密码
     */
    private String password;

    private String identity;
}
