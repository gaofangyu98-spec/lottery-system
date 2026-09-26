package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 奖品状态操作器
 * <p>
 * 负责活动-奖品关联表的状态流转（INIT → COMPLETED）。
 * 执行顺序为 sequence=1，与人员状态并行先于活动状态执行。
 */
@Component
public class PrizeOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    /**
     * 奖品状态先执行（与人员并行）
     */
    @Override
    public Integer sequence() {
        return 1;
    }

    /**
     * 判断奖品是否需要状态流转
     * <p>
     * 条件：活动 ID、奖品 ID、目标状态均非空，且当前奖品状态与目标状态不同
     */
    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityPrizeStatusEnum targetPrizeStatus = convertActivityStatusDTO.getTargetPrizeStatus();
        if (null == activityId
                || null == prizeId
                || null == targetPrizeStatus) {
            return false;
        }

        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(activityId, prizeId);
        if (null == activityPrizeDO) {
            return false;
        }

        // 当前奖品状态和目标状态一致，无需处理
        if (targetPrizeStatus.name().equalsIgnoreCase(activityPrizeDO.getStatus())) {
            return false;
        }

        return true;
    }

    /**
     * 执行奖品状态更新
     */
    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        Long prizeId = convertActivityStatusDTO.getPrizeId();
        ActivityPrizeStatusEnum targetPrizeStatus = convertActivityStatusDTO.getTargetPrizeStatus();
        try {
            activityPrizeMapper.updateStatus(activityId, prizeId, targetPrizeStatus.name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
