package com.example.lotterysystem.common.utils;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;

/**
 * 图形验证码工具类
 * <p>基于 Hutool 生成纯数字的线段干扰型图形验证码。</p>
 */
public class CaptchaUtil {

    /**
     * 生成指定长度的纯数字验证码
     *
     * @param length 验证码位数
     * @return 数字验证码字符串
     */
    public static String generateCaptchaCode(int length) {

        // 自定义纯数字的验证码（随机 length 位数字，可重复）
        RandomGenerator randomGenerator = new RandomGenerator("0123456789", length);
        LineCaptcha lineCaptcha = cn.hutool.captcha.CaptchaUtil.createLineCaptcha(200, 100);
        lineCaptcha.setGenerator(randomGenerator);
        // 重新生成code
        lineCaptcha.createCode();
        return lineCaptcha.getCode();
    }
}
