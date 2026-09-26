package com.example.lotterysystem.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作封装工具类
 * <p>基于 {@link StringRedisTemplate} 封装 String 类型常用读写操作，
 * 所有方法均做异常兜底，Redis 异常时返回 false / null 而非抛异常。</p>
 *
 * @author: yibo
 */
@Configuration
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);


    /**
     * RedisTemplate :  先将被存储的数据转换成 字节数组（不可读），再存储到redis中，读取的时候按照字节数组读取
     * StringRedisTemplate ： 直接存放的就是 string (可读)
     * 项目背景：String,String
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // --------------- String ----------------------

    /**
     * 设置值
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, String value) {
        try {
            stringRedisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("RedisUtil error, set({}, {})", key, value, e);
            return false;
        }
    }

    /**
     * 设置值（设置过期时间）
     *
     * @param key
     * @param value
     * @param time   单位：秒
     * @return
     */
    public boolean set(String key, String value, Long time) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            logger.error("RedisUtil error, set({}, {}, {})", key, value, time, e);
            return false;
        }
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public String get(String key) {
        try {
            return StringUtils.hasText(key)
                    ? stringRedisTemplate.opsForValue().get(key)
                    : null;
        } catch (Exception e) {
            logger.error("RedisUtil error, get({})", key, e);
            return null;
        }
    }

    /**
     * 删除值
     *
     * @param key
     * @return
     */
    public boolean del(String... key) {
        try {
            if (null != key && key.length > 0) {
                if (key.length == 1) {
                    stringRedisTemplate.delete(key[0]);
                } else {
                    stringRedisTemplate.delete(
                            (Collection<String>)CollectionUtils.arrayToList(key)
                    );
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("RedisUtil error, del({})", key, e);
            return false;
        }
    }

    /**
     * 判断key是否存在
     *
     * @param key
     * @return
     */
    public boolean hasKey(String key) {
        try {
            return StringUtils.hasText(key)
                    ? stringRedisTemplate.hasKey(key)
                    : false;
        } catch (Exception e) {
            logger.error("RedisUtil error, hasKey({})", key, e);
            return false;
        }

    }

}
