package com.example.lotterysystem.service.activitystatus.operater;

import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

/**
 * 活动状态操作器抽象基类
 * <p>
 * 定义状态机中的每个状态节点的契约：
 * - sequence：控制执行顺序（小的先执行）
 * - needConvert：判断当前是否需要执行状态转换
 * - convert：执行状态转换
 * <p>
 * 各具体 Operator（活动/奖品/人员）继承本类并实现上述方法，
 * 由 ActivityStatusManagerImpl 统一调度。
 */
public abstract class AbstractActivityOperator {

    /**
     * 控制处理顺序，数字越小越先执行
     *
     * @return 顺序编号
     */
    public abstract Integer sequence();

    /**
     * 判断是否需要执行状态转换
     *
     * @param convertActivityStatusDTO 状态转换参数
     * @return true=需要转换，false=跳过
     */
    public abstract Boolean needConvert(ConvertActivityStatusDTO convertActivityStatusDTO);

    /**
     * 执行状态转换
     *
     * @param convertActivityStatusDTO 状态转换参数
     * @return true=转换成功，false=转换失败
     */
    public abstract Boolean convert(ConvertActivityStatusDTO convertActivityStatusDTO);

}
