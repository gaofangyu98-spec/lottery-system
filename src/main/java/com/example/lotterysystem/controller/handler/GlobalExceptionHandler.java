package com.example.lotterysystem.controller.handler;

import com.example.lotterysystem.common.errorcode.GlobalErrorCodeConstants;
import com.example.lotterysystem.common.exception.ControllerException;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.pojo.CommonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>统一拦截 Controller / Service 抛出的异常，记录日志并转换为统一响应 CommonResult，
 * 避免堆栈信息直接暴露给前端。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 兜底处理未知异常
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> exceptionHandler(Exception e) {
        logger.error("服务错误: ",e);
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVICE_ERROR.getCode(),e.getMessage()
        );
    }

    /**
     * 处理 Controller 层业务异常
     */
    @ExceptionHandler(value = ControllerException.class)
    public CommonResult<?> controllerException(ControllerException e) {
        // 打错误日志
        logger.error("controllerException:", e);
        // 构造错误结果
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVICE_ERROR.getCode(),
                e.getMessage());
    }

    /**
     * 处理 Service 层业务异常
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> exception(Exception e) {
        // 打错误日志
        logger.error("服务异常:", e);
        // 构造错误结果
        return CommonResult.error(
                GlobalErrorCodeConstants.INTERNAL_SERVICE_ERROR.getCode(),
                e.getMessage());
    }

}
