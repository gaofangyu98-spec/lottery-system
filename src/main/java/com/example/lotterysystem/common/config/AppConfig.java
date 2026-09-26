package com.example.lotterysystem.common.config;

import com.example.lotterysystem.common.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC 配置类
 * <p>注册登录拦截器 {@link LoginInterceptor}，并配置不需要登录即可访问的白名单路径。</p>
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    /** 登录拦截器放行的白名单路径（静态资源 + 登录/注册/发送验证码/活动与奖品创建等公开接口） */
    private final List<String> excludes = Arrays.asList(
            "/**/*.html",
            "/css/**",
            "/js/**",
            "/pic/**",
            "/*.webp",
            "/*.jpg",
            "/*.webp",
            "/*.png",
            "/favicon.ico",
            "/**/login",
            "/register",
            "/verification-code/send",
            "/prize/create",
            "/prize/find-list",
            "/activity/create",
            "/activity/find-list"
           // "/winning-records/show"
    );

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludes);
    }
}
