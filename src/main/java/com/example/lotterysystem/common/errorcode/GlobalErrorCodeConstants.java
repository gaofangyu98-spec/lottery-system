package com.example.lotterysystem.common.errorcode;


/**
 * 全局错误码常量
 * <p>定义系统级通用错误码（成功、系统异常、未知错误等）。</p>
 */
public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(200, "成功");

    ErrorCode INTERNAL_SERVICE_ERROR = new ErrorCode(500, "系统异常");

    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");

}
