package com.example.lotterysystem;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.Key;

/**
 * JWT 密钥生成测试：生成 HS256 密钥并输出 Base64 编码。
 *
 * @author: yibo
 */
@SpringBootTest
public class JWTTest {

    /**生成密钥*/
    @Test
    public void genKey(){
        // 创建了一个密钥对象，使用HS256签名算法。
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        // 将密钥编码为Base64字符串。
        String secretString = Encoders.BASE64.encode(key.getEncoded());
        System.out.println(secretString);
        // 结果：dVnsmy+SIX6pNptQdeclDSJ26EMSPEIhvZYKBTTug4k=
    }

}
