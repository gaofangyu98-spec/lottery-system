package com.example.lotterysystem.controller;


import com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.controller.result.BaseUserInfoResult;
import com.example.lotterysystem.controller.result.UserLoginResult;
import com.example.lotterysystem.controller.result.UserRegisterResult;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.VerificationCodeService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户相关接口
 * <p>提供用户注册、发送短信验证码、密码登录、短信验证码登录、按身份查询用户列表等能力。</p>
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    //定义日志
    private static final Logger logger =LoggerFactory.getLogger(UserController.class);

    /**
     * 用户注册
     */
    @RequestMapping("/register")
    public CommonResult<UserRegisterResult> userRegister(@Validated @RequestBody UserRegisterParam param){
        logger.info("userRegister UserRegisterParam:{}", JacksonUtil.writeValueAsString(param));
        UserRegisterDTO userRegisterDTO = userService.register(param);
        return CommonResult.success(converToRegisterResult(userRegisterDTO));

    }

    /**
     * 发送登录/注册短信验证码
     */
    @RequestMapping("/verification-code/send")
    public CommonResult<Boolean> sendVerificationCode(String phoneNumber) {
        logger.info("sendVerificationCode phoneNumber: {}", phoneNumber);
        verificationCodeService.sendVerificationCode(phoneNumber);
        return CommonResult.success(Boolean.TRUE);
    }

    /**
     * 账号密码登录
     */
    @RequestMapping("/password/login")
    public CommonResult<UserLoginResult> userPasswordLogin(@Validated @RequestBody UserPasswordLoginParam param) {
        logger.info("userLogin UserPasswordLoginParam:{}", JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(converToLoginResult(userLoginDTO));
    }

    /**
     * 短信验证码登录
     */
    @RequestMapping("/message/login")
    public CommonResult<UserLoginResult> shortMessageLogin(@Validated @RequestBody ShortMessageLoginParam param) {
        logger.info("userLogin UserPasswordLoginParam:{}", JacksonUtil.writeValueAsString(param));
        UserLoginDTO userLoginDTO = userService.login(param);
        return CommonResult.success(converToLoginResult(userLoginDTO));
    }

    /**
     * 按身份查询用户列表；identity 为空时查询全部用户
     */
    @RequestMapping("/base-user/find-list")
    public CommonResult<List<BaseUserInfoResult>> findBaseUserInfoList(String identity) {
        logger.info("findBaseUserInfoList identity:{}", identity);
        List<UserDTO> userDTOList = null;
        if (!StringUtils.hasLength(identity)) {
            // 未指定身份，查询全部用户
            userDTOList = userService.findUserList(null);

        } else if (null != UserIdentityEnum.fromName(identity)) {
            userDTOList = userService.findUserList(UserIdentityEnum.fromName(identity));
        }
        return CommonResult.success(converToBaseUserInfoResult(userDTOList));
    }

    private List<BaseUserInfoResult> converToBaseUserInfoResult(List<UserDTO> userDTOList) {
        if (CollectionUtils.isEmpty(userDTOList)) {
            return Arrays.asList();
        }
        return userDTOList.stream()
                .map(userDTO -> {
                    BaseUserInfoResult baseUserInfoResult = new BaseUserInfoResult();
                    baseUserInfoResult.setUserId(userDTO.getUserId());
                    baseUserInfoResult.setUserName(userDTO.getUserName());
                    baseUserInfoResult.setIdentity(userDTO.getIdentity().name());
                    return baseUserInfoResult;
                }).collect(Collectors.toList());
    }


    private UserLoginResult converToLoginResult(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.LOGIN_ERROR);
        }
        UserLoginResult userLoginResult = new UserLoginResult();
        userLoginResult.setToken(userLoginDTO.getToken());
        userLoginResult.setIdentity(userLoginDTO.getIdentity().name());
        return userLoginResult;
    }

    private UserRegisterResult converToRegisterResult(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            throw new ControllerException(ControllerErrorCodeConstants.REGISTER_ERROR);
        }
        UserRegisterResult userRegisterResult = new UserRegisterResult();
        userRegisterResult.setUserId(userRegisterDTO.getUserId());
        return userRegisterResult;
    }

}
