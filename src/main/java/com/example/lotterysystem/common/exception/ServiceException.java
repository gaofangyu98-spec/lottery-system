package com.example.lotterysystem.common.exception;


import com.example.lotterysystem.common.errorcode.ErrorCode;
import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义 Service 层异常
 * <p>继承 RuntimeException，用于在业务逻辑校验失败时携带错误码抛出，
 * 由全局异常处理器统一捕获并转换为统一响应。</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {

    /**
     * @see com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants
     */
    //异常码
    private Integer code;

    //异常信息
    private String message;

    //Service 下的错误码可以设置 Service 下的异常信息
    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
