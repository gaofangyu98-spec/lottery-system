package com.example.lotterysystem.service;

import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.service.dto.WinningRecordDTO;

import java.util.List;

/**
 * 抽奖服务接口
 * <p>
 * 定义抽奖流程的核心操作：发起抽奖、参数校验、保存中奖记录、删除/查询中奖记录。
 */
public interface DrawPrizeService {

    /**
     * 发起抽奖：发送抽奖请求消息到 MQ（异步处理）
     *
     * @param param 抽奖请求参数
     */
    void drawPrize(DrawPrizeParam param);

    /**
     * 校验抽奖请求参数是否有效
     *
     * @param param 抽奖请求参数
     * @return true=校验通过，false=校验失败
     */
    boolean checkDrawPrizeParam(DrawPrizeParam param);

    /**
     * 保存中奖记录（写数据库 + 写 Redis 缓存）
     *
     * @param param 抽奖请求参数
     * @return 已保存的中奖记录列表
     */
    List<WinningRecordDO> savewinnerRecords(DrawPrizeParam param);

    /**
     * 删除中奖记录（数据库 + 缓存双删）
     *
     * @param activityId 活动 ID
     * @param prizeId    奖品 ID（可为 null）
     */
    void deleteRecords(Long activityId, Long prizeId);

    /**
     * 查询中奖记录（优先缓存，未命中查库）
     *
     * @param param 查询参数
     * @return 中奖记录 DTO 列表
     */
    List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param);
}
