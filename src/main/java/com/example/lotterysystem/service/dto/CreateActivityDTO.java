package com.example.lotterysystem.service.dto;

import lombok.Data;

/**
 * 创建活动结果 DTO
 * <p>
 * 创建活动接口的返回值，仅包含新创建的活动 ID。
 */
@Data
public class CreateActivityDTO {

    /**
     * 活动id
     */
    private Long activityId;


}
