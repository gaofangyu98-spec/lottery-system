package com.example.lotterysystem;

import com.example.lotterysystem.service.VerificationCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 验证码服务测试：发送验证码并从缓存读取。
 *
 * @author: yibo
 */
@SpringBootTest
public class VerificationCodeServiceTest {
    @Autowired
    private VerificationCodeService verificationCodeService;


    @Test
    void testSend() {
        verificationCodeService.sendVerificationCode("15129270506");
        System.out.println(
                verificationCodeService.getVerificationCode("15129270506")
        );
    }

}
