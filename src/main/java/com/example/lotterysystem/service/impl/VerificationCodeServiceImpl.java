package com.example.lotterysystem.service.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.*;
import com.example.lotterysystem.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 验证码服务实现类
 * <p>
 * 负责生成短信验证码并存入 Redis（60 秒有效期），登录时从 Redis 取出进行校验。
 * Redis key 统一加业务前缀 VERIFICATION_CODE_ 以区分不同业务。
 */
@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {

    /**
     * Redis key 业务前缀，避免不同业务的 key 冲突
     */
    private static final String VERIFICATION_CODE_PREFIX = "VERIFICATION_CODE_";
    /**
     * 验证码有效期（秒），60 秒后自动过期
     */
    private static final Long VERIFICATION_CODE_TIMEOUT = 60L;
    /**
     * 短信模板号（当前未实际调用短信发送，仅在控制台打印验证码用于调试）
     */
    private static final String VERIFICATION_CODE_TEMPLATE_CODE = "SMS_465324787";

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 生成验证码并存储到 Redis
     *
     * @param phoneNumber 手机号
     */
    @Override
    public void sendVerificationCode(String phoneNumber) {
        // 校验手机号格式
        if (!StringUtils.hasLength(phoneNumber) || !RegexUtil.checkMobile(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        // 生成 4 位数字验证码
        String code = CaptchaUtil.generateCaptchaCode(4);
        // TODO: 此处为调试用，生产环境应通过短信网关发送验证码
        redisUtil.set(VERIFICATION_CODE_PREFIX + phoneNumber, code, VERIFICATION_CODE_TIMEOUT);
    }

    /**
     * 从 Redis 获取已发送的验证码
     *
     * @param phoneNumber 手机号
     * @return Redis 中存储的验证码，不存在则返回 null
     */
    @Override
    public String getVerificationCode(String phoneNumber) {
        if (!StringUtils.hasLength(phoneNumber) || !RegexUtil.checkMobile(phoneNumber)) {
            throw new ServiceException(ServiceErrorCodeConstants.PHONE_NUMBER_ERROR);
        }
        return (String) redisUtil.get(VERIFICATION_CODE_PREFIX + phoneNumber);
    }
}
