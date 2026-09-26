package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.dao.dataobject.ActivityUserDO;
import com.example.lotterysystem.dao.mapper.ActivityUserMapper;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 参与人员状态操作器
 * <p>
 * 负责活动-人员关联表的状态流转（INIT → COMPLETED）。
 * 执行顺序为 sequence=1，与奖品状态并行先于活动状态执行。
 * 批量更新中奖人员的状态。
 */
@Component
public class UserOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityUserMapper activityUserMapper;

    /**
     * 人员状态先执行（与奖品并行）
     */
    @Override
    public Integer sequence() {
        return 1;
    }

    /**
     * 判断人员是否需要状态流转
     * <p>
     * 条件：活动 ID、人员 ID 列表、目标状态均非空，
     * 且查询到的关联人员中存在状态不等于目标状态的记录
     */
    @Override
    public Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetUserStatus = convertActivityStatusDTO.getTargetUserStatus();
        if (null == activityId
             || CollectionUtils.isEmpty(userIds)
             || null == targetUserStatus) {
            return false;
        }

        List<ActivityUserDO> activityUserDOList = activityUserMapper.batchselectByAUIds(activityId,
                                                                                        userIds);

        if (CollectionUtils.isEmpty(activityUserDOList)) {
            return false;
        }

        // 只要有一个人员状态与目标状态不同，就需要批量更新
        for (ActivityUserDO activityUserDO : activityUserDOList) {
            if (activityUserDO.getStatus()
                    .equalsIgnoreCase(targetUserStatus.name())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 批量执行人员状态更新
     */
    @Override
    public Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO) {
        Long activityId = convertActivityStatusDTO.getActivityId();
        List<Long> userIds = convertActivityStatusDTO.getUserIds();
        ActivityUserStatusEnum targetUserStatus = convertActivityStatusDTO.getTargetUserStatus();
        try {
            activityUserMapper.batchUpdateStatus(activityId,
                                                userIds,
                                                targetUserStatus.name());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
