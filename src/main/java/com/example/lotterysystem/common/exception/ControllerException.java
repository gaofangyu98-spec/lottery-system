package com.example.lotterysystem.common.exception;


import com.example.lotterysystem.common.errorcode.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 自定义 Controller 层异常
 * <p>继承 RuntimeException，用于在接口参数校验等场景携带错误码抛出，
 * 由全局异常处理器统一捕获并转换为统一响应。</p>
 */
@AllArgsConstructor
@NoArgsConstructor  //为了序列化，创建无参构造函数
@Data
@EqualsAndHashCode(callSuper = true)
public class ControllerException extends RuntimeException{

    /**
     * @see com.example.lotterysystem.common.errorcode.ControllerErrorCodeConstants
     */
    //异常码
    private Integer code;

    //异常信息
    private String message;

    //controller 下的错误码可以设置 controller 下的异常信息
    public ControllerException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
