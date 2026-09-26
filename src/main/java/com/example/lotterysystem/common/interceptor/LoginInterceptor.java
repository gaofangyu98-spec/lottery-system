package com.example.lotterysystem.common.interceptor;
import com.example.lotterysystem.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * <p>在进入 Controller 之前拦截请求，校验请求头 user_token 中的 JWT：
 * 解析失败（缺失/过期/被篡改）则直接返回 401 并中断请求；校验通过才放行。</p>
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头取出登录令牌
        String token = request.getHeader("user_token");
        log.info("获取路径: {}", request.getRequestURI());
        log.info("从Header获取token: {}", token);
        // 校验 token：验签 + 校验过期时间
        Claims claims = JWTUtil.parseJWT(token);
        if (null == claims) {
            log.error("解析JWT令牌失败！");
            // 未登录或令牌非法，返回 401 未授权
            response.setStatus(401);
            return false;
        }
        // 校验通过，放行进入 Controller
        log.info("令牌通过，放行");
        return true;
    }
}
