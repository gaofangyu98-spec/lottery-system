package com.example.lotterysystem.controller.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建奖品请求参数
 *
 * @author: yibo
 */
@Data
public class CreatePrizeParam implements Serializable {

    /**
     * 奖品名
     */
    @NotBlank(message = "奖品名不能为空！")
    private String prizeName;
    /**
     * 描述
     */
    private String description;
    /**
     * 价格
     */
    @NotNull(message = "奖品价格不能为空！")
    private BigDecimal price;

}
