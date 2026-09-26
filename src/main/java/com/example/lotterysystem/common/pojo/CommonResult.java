package com.example.lotterysystem.common.pojo;

import com.example.lotterysystem.common.errorcode.ErrorCode;
import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Objects;


/**
 * 统一响应结果封装
 * <p>所有 Controller 返回值统一包装为该结构，包含业务状态码 code、数据 data、提示 msg。</p>
 */
@Data
public class CommonResult<T> implements Serializable {

    /**
     * @see ErrorCode#getCode()
     */
    private Integer code;

    /**
     * 返回数据
     */
    private T data;

    /**
     * @see ErrorCode#getMsg()
     */
    private String msg;

    /**
     * 将返回的数据封装成 CommonResult
     */
    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = GlobalErrorCodeConstants.SUCCESS.getCode();
        result.data = data;
        result.msg = "";
        return result;
    }

    /**
     * 构造错误响应（禁止传入成功码）
     */
    public static <T> CommonResult<T> error(Integer code, String msg) {
        Assert.isTrue(!GlobalErrorCodeConstants.SUCCESS.getCode().equals(code),"code 是错误的");
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = msg;
        return result;
    }

    /**
     * 依据错误码枚举构造错误响应
     */
    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMsg());
    }

    /**
     * 依据已有响应对象复制其 code/msg 构造错误响应
     */
    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMsg());
    }

    /**
     * 判断业务状态码是否为成功
     */
    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, GlobalErrorCodeConstants.SUCCESS.getCode());
    }
}
