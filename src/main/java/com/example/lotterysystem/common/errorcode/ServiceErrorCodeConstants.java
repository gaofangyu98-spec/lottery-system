package com.example.lotterysystem.common.errorcode;


/**
 * Service 层错误码常量
 * <p>按人员、活动、抽奖、图片等模块划分业务错误码。</p>
 */
public interface ServiceErrorCodeConstants {

    // ------ 人员模块错误码 --------

    ErrorCode REGISTER_INFO_IS_EMPTY = new ErrorCode(100,"注册信息不能为空");
    ErrorCode MAIL_ERROR = new ErrorCode(101,"邮箱错误");
    ErrorCode PHONE_NUMBER_ERROR = new ErrorCode(102,"手机号码错误");
    ErrorCode IDENTITY_ERROR = new ErrorCode(103,"用户身份错误");
    ErrorCode PASSWORD_ERROR = new ErrorCode(104,"密码错误");
    ErrorCode PASSWORD_FORMAT_ERROR = new ErrorCode(105,"密码格式错误");
    ErrorCode MAIL_IS_EMPTY = new ErrorCode(106,"邮箱不能为空");
    ErrorCode PHONE_NUMBER_IS_EMPTY = new ErrorCode(107,"手机号码不能为空");
    ErrorCode MAIL_IS_USED = new ErrorCode(108,"邮箱已被使用");
    ErrorCode PHONE_NUMBER_IS_USED = new ErrorCode(109,"手机号码已被使用");
    ErrorCode LOGIN_NOT_EXIST = new ErrorCode(110,"登录方式不存在");
    ErrorCode MAIL_OR_PHONE_ERROR = new ErrorCode(111,"邮箱或手机号码错误");
    ErrorCode USER_NOT_EXIST = new ErrorCode(112,"用户不存在");
    ErrorCode VERIFICATION_CODE_ERROR = new ErrorCode(113,"验证码错误");



    // ------ 活动模块错误码 --------

    ErrorCode ACTIVITY_INFO_ERROR = new ErrorCode(200,"活动信息错误");
    ErrorCode ACTIVITY_USER_ERROR = new ErrorCode(201,"活动人员错误");
    ErrorCode ACTIVITY_PRIZE_ERROR = new ErrorCode(202,"活动奖品错误");
    ErrorCode ACTIVITY_USER_PRIZE_ERROR = new ErrorCode(203,"活动人员奖品错误");
    ErrorCode ACTIVITY_PRIZE_TIERS_ERROR = new ErrorCode(204,"奖品等奖错误");
    ErrorCode ACTIVITY_OR_PRIZE_IS_EMPTY = new ErrorCode(205,"活动或奖品不能为空");
    ErrorCode ACTIVITY_COMPLETED = new ErrorCode(206,"活动已结束");
    ErrorCode ACTIVITY_STATUS_CONVERT_ERROR = new ErrorCode(207,"活动状态转换错误");
    ErrorCode CACHE_ACTIVITY_ID_IS_EMPTY  = new ErrorCode(208,"缓存活动ID不能为空");
    ErrorCode CACHE_ACTIVITY_ID_ERROR = new ErrorCode(209,"缓存活动ID错误");




    // ------ 抽奖错误码 --------
    ErrorCode ACTIVITY_PRIZE_COMPLETED = new ErrorCode(300,"奖品已抽完");
    ErrorCode WINNER_PRIZE_AMOUNT_ERROR =  new ErrorCode(301,"中奖奖品数量与中奖人数不匹配");



    // ------ 图片错误码 --------

    ErrorCode PIC_UPLOAD_ERROR = new ErrorCode(500,"图片上传失败");


}
