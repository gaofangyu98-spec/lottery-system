package com.example.lotterysystem.common.utils;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Jackson JSON 序列化/反序列化工具类
 * <p>全局复用同一个线程安全的 {@link ObjectMapper}，并把受检异常统一包装为 RuntimeException，
 * 简化调用方的异常处理。</p>
 */
@NoArgsConstructor
public class JacksonUtil {

    private final static ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
    }

    private static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;

    }

    /**
     * 执行解析动作，只捕获 JacksonException
     */
    private static <T > T tryParse(Callable<T> parser) {
        return tryParse(parser, JacksonException.class);
    }

    /**
     * 执行解析动作，把匹配 check 类型的异常包装为运行时异常
     */
    private static <T > T tryParse(Callable<T> parser, Class<? extends Exception> check){
        try {
            return parser.call();
        } catch (Exception ex) {
            if (check.isAssignableFrom(ex.getClass())) {
                throw new RuntimeException("JSON解析失败",ex);
            }
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 序列化为 JSON 字符串
     * @param value 待序列化对象
     * @return JSON 字符串
     */
    public static String writeValueAsString(Object value) {
        return JacksonUtil.tryParse(()->getObjectMapper().writeValueAsString(value));
    }

    /**
     * 反序列化为指定类型对象
     * @param content JSON 字符串
     * @param valueType 目标类型
     * @return 反序列化后的对象
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        return JacksonUtil.tryParse(()->getObjectMapper().readValue(content, valueType));
    }


    /**
     * 反序列化为 List&lt;E&gt;
     * @param content JSON 字符串
     * @param paramClasses List 元素类型
     * @return 反序列化后的 List
     */
    public static <T> T readListValue(String content, Class<?> paramClasses) {
        // 构造 List<paramClasses> 的泛型类型，避免类型擦除导致反序列化失败
        JavaType javaType = JacksonUtil.getObjectMapper().getTypeFactory()
                .constructParametricType(List.class, paramClasses);
        return JacksonUtil.tryParse(() -> {
            return JacksonUtil.getObjectMapper().readValue(content, javaType);
        });
    }

}
