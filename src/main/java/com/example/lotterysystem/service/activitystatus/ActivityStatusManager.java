package com.example.lotterysystem.service.activitystatus;

import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

/**
 * 活动状态管理器接口
 * <p>
 * 基于责任链 / 策略模式实现活动相关状态机流转。
 * 将活动、奖品、人员三类状态的转换逻辑封装为独立的 Operator，
 * 由本接口统一调度，解决硬编码状态流转导致的扩展性差、维护困难问题。
 */
public interface ActivityStatusManager {

    /**
     * 正向状态流转（抽奖成功后调用）
     * <p>
     * 按 sequence 顺序依次执行各 Operator 的状态转换：
     * 先人员/奖品（sequence=1），后活动（sequence=2）。
     * 活动状态仅在所有奖品都抽完后才会变更为 COMPLETED。
     *
     * @param convertActivityStatusDTO 状态转换参数（含目标状态）
     */
    void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);


    /**
     * 回滚状态流转（抽奖异常后调用）
     * <p>
     * 遍历所有 Operator 执行回滚，将状态恢复为初始值：
     * 奖品→INIT，人员→INIT，活动→RUNNING。
     * 回滚不区分顺序，因为奖品都恢复为 INIT 后活动必然回到 RUNNING。
     *
     * @param convertActivityStatusDTO 状态转换参数（含回滚目标状态）
     */
    void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);

}
