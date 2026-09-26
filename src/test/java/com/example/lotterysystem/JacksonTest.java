package com.example.lotterysystem;

import com.example.lotterysystem.common.pojo.CommonResult;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * Jackson 序列化/反序列化测试：同时验证原生 ObjectMapper 与封装的 JacksonUtil。
 *
 * @author: yibo
 */
@SpringBootTest
public class JacksonTest {



    @Test
    void jacksonTest() {

        ObjectMapper objectMapper = new ObjectMapper();

        CommonResult<String> result = CommonResult.error(500, "系统错误");
        String str;

        // 序列化
        try {
            str = objectMapper.writeValueAsString(result);
            System.out.println(str);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 反序列化
        try {
            CommonResult<String> readResult = objectMapper.readValue(str,
                    CommonResult.class);
            System.out.println(readResult.getCode() + readResult.getMsg());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // List 序列化
        List<CommonResult<String>> commonResults = Arrays.asList(
                CommonResult.success("success1"),
                CommonResult.success("success2")
        );
        try {
            str = objectMapper.writeValueAsString(commonResults);
            System.out.println(str);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // List 反序列化
        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(List.class, CommonResult.class);
        try {
            commonResults = objectMapper.readValue(str, javaType);
            for (CommonResult<String> commonResult : commonResults) {
                System.out.println(commonResult.getData());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void JacksonUtilTest() {

        CommonResult<String> result = CommonResult.success("success");
        String str;

        // 序列化
        str = JacksonUtil.writeValueAsString(result);
        System.out.println(str);

        // 反序列化
        result = JacksonUtil.readValue(str, CommonResult.class);
        System.out.println(result.getData());

        // 序列化List
        List<CommonResult<String>> commonResults = Arrays.asList(
                CommonResult.success("success1"),
                CommonResult.success("success2")
        );
        str = JacksonUtil.writeValueAsString(commonResults);
        System.out.println(str);

        // 反序列化List
        commonResults = JacksonUtil.readListValue(str, CommonResult.class);
        for (CommonResult<String> commonResult : commonResults) {
            System.out.println(commonResult.getData());
        }
    }


}
