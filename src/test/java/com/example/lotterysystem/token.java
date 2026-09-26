package com.example.lotterysystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;

/**
 * JWT 密钥生成测试（与 JWTTest 类似，输出 Base64 编码密钥）。
 */
@SpringBootTest
public class token {


    @Test
    void key() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.println("------------------------");
        System.out.println( key);
        System.out.println("------------------------");
        String str = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("------------------------");
        System.out.println( str);
        System.out.println("------------------------");
    }
}
