package com.example.lotterysystem.common.errorcode;


/**
 * Controller 层错误码常量
 * <p>定义接口入参校验、请求处理失败等场景的错误码。</p>
 */
public interface ControllerErrorCodeConstants {
    //--------人员模块错误码----------
    ErrorCode REGISTER_ERROR = new ErrorCode(100,"注册失败");

    ErrorCode LOGIN_ERROR = new ErrorCode(101,"登录失败") ;


    //--------活动模块错误码----------
    ErrorCode CREATE_ACTIVITY_ERROR = new ErrorCode(201,"创建活动失败");
    ErrorCode FIND_ACTIVITY_LIST_ERROR = new ErrorCode(202,"获取活动列表失败");
    ErrorCode GET_ACTIVITY_DETAIL_ERROR = new ErrorCode(203,"获取活动详情失败");


    //--------奖品模块错误码----------

    ErrorCode PRIZE_LIST_ERROR = new ErrorCode(300,"获取奖品列表失败");



}
