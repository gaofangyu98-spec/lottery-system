package com.example.lotterysystem.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JWTUtil;
import com.example.lotterysystem.common.utils.RegexUtil;
import com.example.lotterysystem.controller.param.ShortMessageLoginParam;
import com.example.lotterysystem.controller.param.UserLoginParam;
import com.example.lotterysystem.controller.param.UserPasswordLoginParam;
import com.example.lotterysystem.controller.param.UserRegisterParam;
import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.dataobject.UserDO;
import com.example.lotterysystem.dao.mapper.UserMapper;
import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.VerificationCodeService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.dto.UserLoginDTO;
import com.example.lotterysystem.service.dto.UserRegisterDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * <p>
 * 核心职责：
 * 1. 用户注册（校验邮箱/手机号格式和唯一性、密码 SHA-256 加密、手机号 AES 加密存储）
 * 2. 用户登录（密码登录 / 短信验证码登录两种方式，登录成功签发 JWT）
 * 3. 用户列表查询（可按身份筛选）
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeService verificationCodeService;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 用户注册
     * <p>
     * 密码使用 SHA-256 哈希后存储（不可逆），手机号通过 EncryptTypeHandler 做 AES 加密存储。
     *
     * @param param 注册参数
     * @return 注册结果（含用户 ID）
     */
    @Override
    public UserRegisterDTO register(UserRegisterParam param) {
        checkRegisterDTO(param);
        UserDO userDO = new UserDO();
        userDO.setUserName(param.getUserName());
        userDO.setIdentity(param.getIdentity());
        userDO.setEmail(param.getEmail());
        if (StringUtils.hasLength(param.getPassword())) {
            userDO.setPassword(DigestUtil.sha256Hex(param.getPassword()));
        }
        // 手机号使用 Encrypt 包装，由 EncryptTypeHandler 在写入数据库时自动 AES 加密
        userDO.setPhoneNumber(new Encrypt(param.getPhoneNumber()));

        userMapper.insert(userDO);
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUserId(userDO.getId());
        return userRegisterDTO;
    }

    /**
     * 用户登录入口：根据参数类型分发到密码登录或短信登录
     *
     * @param param 登录参数（多态：密码登录 / 短信登录）
     * @return 登录结果（含 JWT token + 用户身份）
     */
    @Override
    public UserLoginDTO login(UserLoginParam param) {
        UserLoginDTO userLoginDTO;
        if (param instanceof UserPasswordLoginParam userPasswordLoginParam) {
            userLoginDTO = loginByPassword(userPasswordLoginParam);
        } else if (param instanceof ShortMessageLoginParam shortMessageLoginParam) {
            userLoginDTO = loginByShortMessage(shortMessageLoginParam);
        } else {
            throw new ServiceException(ServiceErrorCodeConstants.LOGIN_NOT_EXIST);
        }
        return userLoginDTO;
    }

    /**
     * 查询用户列表（可按身份筛选）
     *
     * @param identity 用户身份枚举（null 表示查询全部）
     * @return 用户 DTO 列表
     */
    @Override
    public List<UserDTO> findUserList(UserIdentityEnum identity) {
        String identityString = null == identity ? null : identity.name();
        List<UserDO> userDOList = userMapper.selectUserList(identityString);
        return userDOList.stream()
                .map(userDO -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setUserId(userDO.getId());
                    userDTO.setUserName(userDO.getUserName());
                    userDTO.setEmail(userDO.getEmail());
                    userDTO.setPhoneNumber(userDO.getPhoneNumber().getValue());
                    userDTO.setIdentity(UserIdentityEnum.fromName(userDO.getIdentity()));
                    return userDTO;
                }).collect(Collectors.toList());
    }

    /**
     * 密码登录
     * <p>
     * 支持邮箱或手机号作为登录名，密码做 SHA-256 比对。
     * 可选校验强制身份（如管理员登录场景）。
     *
     * @param param 密码登录参数
     * @return 登录结果（含 JWT token + 用户身份）
     */
    public UserLoginDTO loginByPassword(UserPasswordLoginParam param) {
        String loginName = param.getLoginName();
        String password = param.getPassword();
        UserDO userDO;
        if (!StringUtils.hasLength(password)) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        // 根据登录名格式判断是邮箱还是手机号
        if (RegexUtil.checkMail(loginName)) {
            userDO = userMapper.selectByMail(loginName);
        } else if (RegexUtil.checkMobile(loginName)) {
            userDO = userMapper.selectByPhoneNumber(new Encrypt(loginName));
        } else {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_OR_PHONE_ERROR);
        }
        if (userDO == null) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_NOT_EXIST);
        } else if (StringUtils.hasLength(param.getMandatoryIdentity()) && !param.getMandatoryIdentity().equals(userDO.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        } else if (!userDO.getPassword().equals(DigestUtil.sha256Hex(password))) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }

        // 登录成功：生成 JWT 令牌（包含 userId 和 identity）
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDO.getId());
        claims.put("identity", userDO.getIdentity());
        String token = JWTUtil.genJwt(claims);

        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.fromName(userDO.getIdentity()));
        return userLoginDTO;
    }

    /**
     * 短信验证码登录
     * <p>
     * 通过手机号查询用户，再校验 Redis 中的验证码是否匹配。
     *
     * @param param 短信登录参数
     * @return 登录结果（含 JWT token + 用户身份）
     */
    private UserLoginDTO loginByShortMessage(ShortMessageLoginParam param) {
        String loginMobile = param.getLoginMobile();
        String verificationCode = param.getVerificationCode();
        if (!RegexUtil.checkMobile(loginMobile)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }

        UserDO userDO = userMapper.selectByPhoneNumber(new Encrypt(loginMobile));

        if (userDO == null) {
            throw new ServiceException(ServiceErrorCodeConstants.USER_NOT_EXIST);
        } else if (StringUtils.hasLength(param.getMandatoryIdentity()) && !param.getMandatoryIdentity().equals(userDO.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }

        // 从 Redis 获取验证码并比对
        String code = verificationCodeService.getVerificationCode(loginMobile);
        if (!StringUtils.hasLength(code) || !code.equals(verificationCode)) {
            throw new ServiceException(ServiceErrorCodeConstants.VERIFICATION_CODE_ERROR);
        }
        // 登录成功：生成 JWT 令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDO.getId());
        claims.put("identity", userDO.getIdentity());
        String token = JWTUtil.genJwt(claims);
        UserLoginDTO userLoginDTO = new UserLoginDTO();
        userLoginDTO.setToken(token);
        userLoginDTO.setIdentity(UserIdentityEnum.fromName(userDO.getIdentity()));
        return userLoginDTO;
    }

    /**
     * 校验注册参数的合法性
     * <p>
     * 校验维度：邮箱格式、手机号格式、身份合法性、管理员必须设密码、密码格式、
     * 邮箱和手机号是否已被注册。
     *
     * @param param 注册参数
     */
    private void checkRegisterDTO(UserRegisterParam param) {
        if (param == null) {
            throw new ServiceException(ServiceErrorCodeConstants.REGISTER_INFO_IS_EMPTY);
        }
        // 校验邮箱格式
        if (!RegexUtil.checkMail(param.getEmail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_ERROR);
        }
        // 校验手机号格式
        if (!RegexUtil.checkMobile(param.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        // 检查身份是否合法
        if (null == UserIdentityEnum.fromName(param.getIdentity())) {
            throw new ServiceException(ServiceErrorCodeConstants.IDENTITY_ERROR);
        }
        // 管理员必须设置密码
        if (param.getIdentity().equalsIgnoreCase(UserIdentityEnum.ADMIN.name()) && !StringUtils.hasLength(param.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_ERROR);
        }
        // 密码格式校验，最少6位
        if (StringUtils.hasLength(param.getPassword()) && !RegexUtil.checkPassword(param.getPassword())) {
            throw new ServiceException(ServiceErrorCodeConstants.PASSWORD_FORMAT_ERROR);
        }
        // 邮箱是否已被注册
        if (checkMailUsed(param.getEmail())) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_IS_USED);
        }
        // 手机号是否已被注册
        if (checkPhoneUsed(param.getPhoneNumber())) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_IS_USED);
        }

    }

    /**
     * 检查邮箱是否已被使用
     */
    private boolean checkMailUsed(String email) {
        if (!StringUtils.hasLength(email)) {
            throw new ServiceException(ServiceErrorCodeConstants.MAIL_IS_EMPTY);
        }
        int count = userMapper.countByMail(email);
        return count > 0;
    }

    /**
     * 检查手机号是否已被使用
     */
    private boolean checkPhoneUsed(String phoneNumber) {
        if (!StringUtils.hasLength(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_IS_EMPTY);
        }
        int count = userMapper.countByPhoneNumber(new Encrypt(phoneNumber));
        return count > 0;
    }
}
