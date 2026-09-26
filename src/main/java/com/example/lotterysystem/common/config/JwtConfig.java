package com.example.lotterysystem.common.config;

import com.example.lotterysystem.common.utils.JWTUtil;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 配置初始化器。
 * <p>从 application.properties 读取 jwt.secret（Base64 编码的 HMAC 密钥）和
 * jwt.expiration（过期毫秒数），在 Spring 启动时注入到 {@link JWTUtil} 中，
 * 避免在工具类源码中硬编码敏感信息。</p>
 */
@Configuration
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${jwt.secret:}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private long expiration;

    @PostConstruct
    public void init() {
        JWTUtil.init(secret, expiration);
        log.info("JwtConfig 初始化完成");
    }
}
