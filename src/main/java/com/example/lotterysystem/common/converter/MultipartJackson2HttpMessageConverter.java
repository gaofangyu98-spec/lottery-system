package com.example.lotterysystem.common.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

/**
 * 支持 multipart/form-data 文件上传的 Jackson 消息转换器
 * <p>默认 Spring 不会用 Jackson 转换器处理二进制流类型，这里将其注册为
 * application/octet-stream 的处理器；同时禁用所有 write 能力，只负责读取，
 * 避免与默认 JSON 响应转换器冲突。</p>
 *
 * @author: yibo
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    protected MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        // MediaType.APPLICATION_OCTET_STREAM 表示这个转换器用于处理二进制流数据，通常用于文件上传。
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        // 转换器不用于写入（即不用于响应的序列化）
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}