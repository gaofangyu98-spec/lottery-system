package com.example.lotterysystem.common.errorcode;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 统一错误码定义
 * <p>封装一个错误码与对应的错误描述，供各层错误码常量类统一使用。</p>
 */
@Data
@AllArgsConstructor     //全参构造函数
public class ErrorCode {
    /** 错误码 */
    private final Integer code;

    /** 错误描述 */
    private final String msg;

}
