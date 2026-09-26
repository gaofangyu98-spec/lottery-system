package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;

import java.util.List;

/**
 * 用户服务接口
 * <p>
 * 定义用户注册、登录和用户列表查询操作。
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param param 注册参数
     * @return 注册结果（含用户 ID）
     */
    UserRegisterDTO register(UserRegisterParam param);

    /**
     * 用户登录（支持密码登录和短信验证码登录）
     *
     * @param param 登录参数
     * @return 登录结果（含 JWT token）
     */
    UserLoginDTO login(UserLoginParam param);

    /**
     * 查询用户列表（可按身份筛选）
     *
     * @param identity 用户身份（null=全部）
     * @return 用户列表
     */
    List<UserDTO> findUserList(UserIdentityEnum identity);

    /**
     * 密码登录
     *
     * @param param 密码登录参数
     * @return 登录结果（含 JWT token）
     */
    UserLoginDTO loginByPassword(UserPasswordLoginParam param);
}
