package com.example.lotterysystem.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT（JSON Web Token）工具类
 * <p>
 * 用于生成、解析和校验用户登录令牌。登录成功后由服务端下发 JWT，
 * 后续请求携带该令牌，服务端通过 {@link #parseJWT(String)} 校验签名与过期时间，
 * 从而识别用户身份，避免每次请求都查询数据库。
 * </p>
 *
 * @author: yibo
 */
public class JWTUtil {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    /**
     * JWT 签名密钥（Base64 编码），由 Spring 启动时通过 {@link #init} 注入。
     */
    private static String secret;

    /**
     * JWT 过期时间（毫秒），由 Spring 启动时通过 {@link #init} 注入。
     */
    private static long expiration;

    /**
     * Spring 启动时调用，注入 JWT 配置（密钥、过期时间）。
     * 避免在源码中硬编码敏感信息。
     */
    public static void init(String jwtSecret, long jwtExpirationMs) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            logger.warn("JWT secret 未配置，使用默认开发密钥（仅用于本地开发）");
            jwtSecret = "eHl6MTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6MTIzNDU2"; // dev only
        }
        secret = jwtSecret;
        expiration = jwtExpirationMs > 0 ? jwtExpirationMs : 3600000L;
        logger.info("JWT 配置已初始化，过期时间={}ms", expiration);
    }

    private static SecretKey getSecretKey() {
        if (secret == null) {
            secret = "eHl6MTIzNDU2Nzg5MGFiY2RlZmdoaWprbG1ub3BxcnN0dXZ3eHl6MTIzNDU2";
        }
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * 生成 JWT 令牌
     *
     * @param claim 自定义载荷，通常存放用户标识，例如 {"userId": 12, "name":"张三"}
     * @return 签名后的 JWT 字符串
     */
    public static String genJwt(Map<String, Object> claim){
        //签名算法
        String jwt = Jwts.builder()
                .setClaims(claim)             // 自定义内容(载荷)
                .setIssuedAt(new Date())      // 设置签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 设置过期时间
                .signWith(getSecretKey())         // 签名算法
                .compact();
        return jwt;
    }

    /**
     * 解析并校验 JWT 令牌
     * <p>使用固定密钥验签，令牌为空、已过期或被篡改时返回 null。</p>
     *
     * @param jwt 待解析的令牌字符串
     * @return 解析成功返回载荷 Claims，失败返回 null
     */
    public static Claims parseJWT(String jwt){
        if (!StringUtils.hasLength(jwt)){
            return null;
        }
        // 创建解析器, 设置签名密钥
        JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder().setSigningKey(getSecretKey());
        Claims claims = null;
        try {
            //解析token
            claims = jwtParserBuilder.build().parseClaimsJws(jwt).getBody();
        }catch (Exception e){
            // 签名验证失败
            logger.error("解析令牌错误,jwt:{}", jwt, e);
        }
        return claims;

    }

    /**
     * 从 token 载荷中取出 userId 字段
     *
     * @param jwtToken 令牌字符串
     * @return 用户 ID；解析失败或载荷中无 userId 时返回 null
     */
    public static Integer getUserIdFromToken(String jwtToken) {
        Claims claims = JWTUtil.parseJWT(jwtToken);
        if (claims != null) {
            Map<String, Object> userInfo = new HashMap<>(claims);
            return (Integer) userInfo.get("userId");
        }
        return null;
    }
}
