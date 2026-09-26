package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.CreateActivityParam;
import com.example.lotterysystem.controller.param.PageListParam;
import com.example.lotterysystem.service.dto.ActivityDTO;
import com.example.lotterysystem.service.dto.ActivityDetailDTO;
import com.example.lotterysystem.service.dto.CreateActivityDTO;
import com.example.lotterysystem.service.dto.PageListDTO;

/**
 * 活动管理服务接口
 * <p>
 * 定义活动的创建、分页查询、详情查询和缓存刷新操作。
 */
public interface ActivityService {

    /**
     * 创建活动（含关联奖品和参与人员，多表事务）
     *
     * @param param 创建活动参数
     * @return 创建结果（含活动 ID）
     */
    CreateActivityDTO createActivity(CreateActivityParam param);

    /**
     * 活动分页列表查询
     *
     * @param param 分页参数
     * @return 分页结果
     */
    PageListDTO<ActivityDTO> findActivityList(PageListParam param);

    /**
     * 刷新活动缓存（状态机流转后调用）
     *
     * @param activityId 活动 ID
     */
    void cacheActivity(Long activityId);

    /**
     * 查询活动详情（含奖品列表和参与人员列表）
     *
     * @param activityId 活动 ID
     * @return 活动详情
     */
    ActivityDetailDTO getActivityDetail(Long activityId);
}
