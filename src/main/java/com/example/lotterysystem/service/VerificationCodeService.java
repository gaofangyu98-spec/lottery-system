package com.example.lotterysystem.service;

/**
 * 验证码服务接口
 * <p>
 * 负责发送和获取短信验证码。
 */
public interface VerificationCodeService {

    /**
     * 发送验证码（生成并存入 Redis）
     *
     * @param phoneNumber 手机号
     */
    void sendVerificationCode(String phoneNumber);

    /**
     * 获取已发送的验证码（从 Redis 读取）
     *
     * @param phoneNumber 手机号
     * @return Redis 中存储的验证码
     */
    String getVerificationCode(String phoneNumber);
}
