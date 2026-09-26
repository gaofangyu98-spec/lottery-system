package com.example.lotterysystem.service.activitystatus.impl;

import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.service.ActivityService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.activitystatus.operater.AbstractActivityOperator;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 活动状态管理器实现类
 * <p>
 * 核心机制：Spring 自动将所有 AbstractActivityOperator 的子类 Bean 注入到 operatorMap 中
 * （key 为 Bean 名称，value 为 Operator 实例）。
 * 通过遍历 operatorMap，按 sequence 字段控制执行顺序，实现责任链式状态流转。
 * <p>
 * 执行顺序：
 * - sequence=1：人员状态（UserOperator）、奖品状态（PrizeOperator）
 * - sequence=2：活动状态（ActivityOperator，依赖所有奖品抽完才流转）
 */
@Component
public class ActivityStatusManagerImpl implements ActivityStatusManager {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatusManagerImpl.class);

    /**
     * Spring 自动注入所有 AbstractActivityOperator 子类的 Bean，key 为 Bean 名称
     */
    @Autowired
    private final Map<String, AbstractActivityOperator> operatorMap = new HashMap<>();
    @Autowired
    private ActivityService activityService;

    /**
     * 正向状态流转（事务保证原子性）
     * <p>
     * 先执行 sequence=1 的 Operator（人员、奖品），再执行 sequence=2 的 Operator（活动）。
     * 任意 Operator 转换失败则抛出异常，事务回滚。
     * 只要有任何状态发生了变更，最后刷新 Redis 中的活动缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        if (CollectionUtils.isEmpty(operatorMap)) {
            logger.warn("operatorMap 为空！");
            return;
        }
        // 复制一份 operatorMap，遍历时可安全删除已处理的 Operator
        Map<String, AbstractActivityOperator> currMap = new HashMap<>(operatorMap);
        Boolean update = false;
        // 先处理：人员、奖品（sequence=1）
        update = processConvertStatus(convertActivityStatusDTO, currMap, 1);
        // 后处理：活动（sequence=2），活动依赖所有奖品抽完
        update = processConvertStatus(convertActivityStatusDTO, currMap, 2) || update;
        // 如果有状态变更，刷新活动缓存
        if (update) {
            activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
        }
    }

    /**
     * 回滚状态流转
     * <p>
     * 遍历所有 Operator 执行回滚。活动一定会被回滚（因为奖品恢复 INIT 后活动必然未完成）。
     * 回滚完成后刷新活动缓存。
     */
    @Override
    public void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO) {
        for (AbstractActivityOperator operator : operatorMap.values()) {
            operator.convert(convertActivityStatusDTO);
        }
        // 缓存更新
        activityService.cacheActivity(convertActivityStatusDTO.getActivityId());
    }

    /**
     * 按指定 sequence 遍历 Operator 并执行状态转换
     * <p>
     * 只处理 sequence 匹配且 needConvert 返回 true 的 Operator。
     * 转换成功后从 currMap 中移除该 Operator，避免重复处理。
     *
     * @param convertActivityStatusDTO 状态转换参数
     * @param currMap                  待处理的 Operator Map（遍历时会移除已处理项）
     * @param sequence                 目标执行顺序
     * @return 是否有任何状态发生了变更
     */
    private Boolean processConvertStatus(ConvertActivityStatusDTO convertActivityStatusDTO,
                                         Map<String, AbstractActivityOperator> currMap,
                                         int sequence) {
        Boolean update = false;
        Iterator<Map.Entry<String, AbstractActivityOperator>> iterator = currMap.entrySet().iterator();
        while (iterator.hasNext()) {
            AbstractActivityOperator operator = iterator.next().getValue();
            // 跳过不匹配 sequence 或不需要转换的 Operator
            if (operator.sequence() != sequence
                    || !operator.needConvert(convertActivityStatusDTO)) {
                continue;
            }

            // 执行状态转换，失败则抛异常触发事务回滚
            if (!operator.convert(convertActivityStatusDTO)) {
                logger.error("{}状态转换失败！", operator.getClass().getName());
                throw new ServiceException(ServiceErrorCodeConstants.ACTIVITY_STATUS_CONVERT_ERROR);
            }

            // 从待处理 Map 中移除，避免重复处理
            iterator.remove();
            update = true;
        }

        return update;
    }
}
