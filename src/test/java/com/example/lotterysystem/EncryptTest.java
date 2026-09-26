package com.example.lotterysystem;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;

/**
 * 加密工具测试：SHA256 摘要与 AES 对称加解密。
 */
@SpringBootTest
public class EncryptTest {

    /**
     * sha256 加密
     * 密码加密方法
     */
    @Test
    void sha256Test() {
        String encrypt = DigestUtil.sha256Hex("123456");
        System.out.println("sha256 加密后 " + encrypt);
        //8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92
    }

    /**
     * 对称密钥加密
     * 手机号密钥加密方法
     */
    @Test
    void aesTest() {
        //密钥
        byte[] KEYS = "123456789abcdefg".getBytes(StandardCharsets.UTF_8);
        //加密
        AES aes = SecureUtil.aes(KEYS);
        String encrypt = aes.encryptHex("123456");
        System.out.println("AES 加密后 " + encrypt);
        //3014dcb9ee3639535d5d9301b32c840c

        //解密
        System.out.println("AES 解密后 " + SecureUtil.aes(KEYS).decryptStr(encrypt));
        //123456
    }

}
