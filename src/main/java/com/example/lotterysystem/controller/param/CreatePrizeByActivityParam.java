package com.example.lotterysystem.controller.param;

import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 活动关联奖品请求参数（创建活动时使用）
 */
@Data
public class CreatePrizeByActivityParam {

    /**
     * 活动关联的奖品id
     */
    @NotNull(message = "活动关联的奖品id不能为空！")
    private Long prizeId;
    /**
     * 奖品数量
     */
    @NotNull(message = "奖品数量不能为空！")
    private Long prizeAmount;
    /**
     * 奖品等奖
     */
    @NotBlank(message = "奖品等奖不能为空！")
    private String prizeTiers;
}
