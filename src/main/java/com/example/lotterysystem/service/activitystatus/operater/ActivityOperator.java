package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityDO;
import com.example.lotterysystem.dao.mapper.ActivityMapper;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 活动状态操作器
 * <p>
 * 负责活动主表的状态流转（RUNNING → COMPLETED）。
 * 执行顺序为 sequence=2，必须在所有奖品状态流转完成后执行。
 * 仅当所有奖品都已抽完（不存在 INIT 状态的奖品）时，活动才会变为 COMPLETED。
 */
@Component
public class ActivityOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    /**
     * 活动状态最后执行（依赖所有奖品抽完）
     */
    @Override
    public Integer sequence() {
        return 2;
    }

    /**
     * 判断活动是否需要状态流转
     * <p>
     * 条件：活动存在、当前状态与目标状态不同、且所有奖品都已抽完（无 INIT 状态奖品）
     */
    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        ActivityStatusEnum targetStatus = convertActivityStatusDTO.getTargetActivityStatus();
        if (null == activityId
                || null == targetStatus) {
            return false;
        }

        ActivityDO activityDO = activityMapper.selectById(activityId);
        if (null == activityDO) {
            return false;
        }

        // 当前活动状态与传入的目标状态一致，无需处理
        if (targetStatus.name().equalsIgnoreCase(activityDO.getStatus())) {
            return false;
        }

        // 必须所有奖品都抽完后，活动才能变为 COMPLETED
        int count = activityPrizeMapper.countPrize(activityId, ActivityPrizeStatusEnum.INIT.name());
        if (count > 0) {
            return false;
        }

        return true;
    }

    /**
     * 执行活动状态更新
     */
    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        try {
            activityMapper.updateStatus(convertActivityStatusDTO.getActivityId(),
                    convertActivityStatusDTO.getTargetActivityStatus().name());
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
