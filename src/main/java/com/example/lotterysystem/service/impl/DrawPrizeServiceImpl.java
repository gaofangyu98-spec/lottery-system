package com.example.lotterysystem.service.impl;


import com.example.lotterysystem.common.errorcode.ServiceErrorCodeConstants;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.RedisUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.controller.param.ShowWinningRecordsParam;
import com.example.lotterysystem.dao.dataobject.*;
import com.example.lotterysystem.dao.mapper.*;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.dto.WinningRecordDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.EXCHANGE_NAME;
import static com.example.lotterysystem.common.config.DirectRabbitConfig.ROUTING;

/**
 * 抽奖服务实现类
 * <p>
 * 核心职责：
 * 1. 发送抽奖请求消息到 MQ（异步解耦，保证接口快速响应）
 * 2. 校验抽奖参数有效性（活动是否完成、奖品是否抽完、中奖人数是否匹配）
 * 3. 保存中奖记录到数据库，并缓存到 Redis（双维度：奖品维度 + 活动维度）
 * 4. 查询中奖记录（优先查 Redis，未命中再查库并回写缓存）
 * 5. 删除中奖记录（用于异常回滚场景）
 */
@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {

    private static final Logger logger = LoggerFactory.getLogger(DrawPrizeServiceImpl.class);

    /**
     * 中奖记录缓存 key 前缀
     */
    private final String WINNING_RECORDS_PREFIX = "WINNING_RECORDS_";
    /**
     * 中奖记录缓存过期时间（2 天）
     */
    private final Long WINNING_RECORDS_TIMEOUT = 60 * 60 * 24 * 2L;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 发起抽奖：将抽奖请求序列化为消息发送到 RabbitMQ，由 MqReceiver 异步处理
     *
     * @param param 抽奖请求参数（活动 ID、奖品 ID、中奖者列表、中奖时间）
     */
    @Override
    public void drawPrize(DrawPrizeParam param) {
        Map<String, String> map = new HashMap<>();
        map.put("messageId", String.valueOf(UUID.randomUUID()));
        map.put("messageData", JacksonUtil.writeValueAsString(param));
        // 发消息: 交换机、绑定的key、消息体
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING, map);
        logger.info("mq消息发送成功：map={}", JacksonUtil.writeValueAsString(map));
    }

    /**
     * 校验抽奖请求参数是否有效
     * <p>
     * 校验维度：
     * - 活动和奖品是否存在
     * - 活动是否已完成（COMPLETED 状态不可再抽）
     * - 奖品是否已被抽完（COMPLETED 状态不可再抽）
     * - 传入的中奖人数是否与奖品设置数量一致
     * <p>
     * 注意：校验失败不抛异常，而是返回 false 由 MqReceiver 直接丢弃消息。
     *
     * @param param 抽奖请求参数
     * @return true=校验通过，false=校验失败
     */
    @Override
    public boolean checkDrawPrizeParam(DrawPrizeParam param) {

        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        // 奖品是否存在可以从 activity_prize 查询，原因是保存 activity 时做了本地事务，保证一致性
        ActivityPrizeDO activityPrizeDO = activityPrizeMapper.selectByAPId(
                param.getActivityId(), param.getPrizeId());

        // 活动或奖品是否存在
        if (null == activityDO || null == activityPrizeDO) {
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_OR_PRIZE_IS_EMPTY.getMsg());
            return false;
        }

        // 活动是否已完成
        if (activityDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())) {
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_COMPLETED.getMsg());
            return false;
        }

        // 奖品是否已被抽完
        if (activityPrizeDO.getStatus()
                .equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name())) {
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.ACTIVITY_PRIZE_COMPLETED.getMsg());
            return false;
        }

        // 中奖者人数是否和设置奖品数量一致
        if (activityPrizeDO.getPrizeAmount() != param.getWinnerList().size()) {
            logger.info("校验抽奖请求失败！失败原因：{}",
                    ServiceErrorCodeConstants.WINNER_PRIZE_AMOUNT_ERROR.getMsg());
            return false;
        }
        return true;
    }

    /**
     * 保存中奖记录：批量插入数据库 + 写入 Redis 缓存
     * <p>
     * 缓存策略：
     * 1. 奖品维度缓存：key = activityId_prizeId，值为该奖品下的中奖名单
     * 2. 活动维度缓存：key = activityId，值为该活动全量中奖名单
     *    （仅当活动已全部完成时才写入活动维度缓存，避免频繁更新）
     *
     * @param param 抽奖请求参数
     * @return 已保存的中奖记录列表
     */
    @Override
    public List<WinningRecordDO> savewinnerRecords(DrawPrizeParam param) {
        // 查询相关信息：活动、人员、奖品、活动关联奖品
        ActivityDO activityDO = activityMapper.selectById(param.getActivityId());
        List<UserDO> userDOList = userMapper.selectByIds(
                param.getWinnerList()
                        .stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        PrizeDO prizeDO = prizeMapper.selectById(param.getPrizeId());
        ActivityPrizeDO activityPrizeDO =
                activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());

        // 构造中奖者记录，保存
        List<WinningRecordDO> winningRecordDOList = userDOList.stream()
                .map(userDO -> {
                    WinningRecordDO winningRecordDO = new WinningRecordDO();
                    winningRecordDO.setActivityId(activityDO.getId());
                    winningRecordDO.setActivityName(activityDO.getActivityName());
                    winningRecordDO.setPrizeId(prizeDO.getId());
                    winningRecordDO.setPrizeName(prizeDO.getName());
                    winningRecordDO.setPrizeTier(activityPrizeDO.getPrizeTiers());
                    winningRecordDO.setWinnerId(userDO.getId());
                    winningRecordDO.setWinnerName(userDO.getUserName());
                    winningRecordDO.setWinnerEmail(userDO.getEmail());
                    winningRecordDO.setWinnerPhoneNumber(userDO.getPhoneNumber());
                    winningRecordDO.setWinningTime(param.getWinningTime());
                    return winningRecordDO;
                }).collect(Collectors.toList());
        winningRecordMapper.batchInsert(winningRecordDOList);

        // 缓存奖品维度中奖记录（奖品维度的中奖名单）
        cacheWinningRecords(param.getActivityId() + "_" + param.getPrizeId(),
                winningRecordDOList,
                WINNING_RECORDS_TIMEOUT);

        // 缓存活动维度中奖记录：当活动已完成再去存放活动维度中奖记录
        if (activityDO.getStatus()
                .equalsIgnoreCase(ActivityStatusEnum.COMPLETED.name())) {
            // 查询活动维度的全量中奖记录
            List<WinningRecordDO> allList = winningRecordMapper.selectByActivityId(param.getActivityId());
            cacheWinningRecords(String.valueOf(param.getActivityId()),
                    allList,
                    WINNING_RECORDS_TIMEOUT);
        }

        return winningRecordDOList;
    }

    /**
     * 删除中奖记录（数据库 + Redis 缓存双删）
     * <p>
     * 无论是否传入 prizeId，都会删除活动维度的缓存：
     * - 传入 prizeId：奖品未抽奖，需清除活动维度缓存
     * - 未传 prizeId：仅删除活动维度的记录
     *
     * @param activityId 活动 ID
     * @param prizeId    奖品 ID（可为 null）
     */
    @Override
    public void deleteRecords(Long activityId, Long prizeId) {
        if (null == activityId) {
            logger.warn("要删除中奖记录相关的活动id为空！");
            return;
        }
        // 删除数据表
        winningRecordMapper.deleteRecords(activityId, prizeId);

        // 删除缓存（奖品维度、活动维度）
        if (null != prizeId) {
            deleteWinningRecords(activityId + "_" + prizeId);
        }
        deleteWinningRecords(String.valueOf(activityId));
    }

    /**
     * 查询中奖记录：优先查 Redis 缓存，未命中则查数据库并回写缓存
     *
     * @param param 查询参数（活动 ID + 可选奖品 ID）
     * @return 中奖记录 DTO 列表
     */
    @Override
    public List<WinningRecordDTO> getRecords(ShowWinningRecordsParam param) {
        // 查询 redis: 奖品、活动
        String key = null == param.getPrizeId()
                ? String.valueOf(param.getActivityId())
                : param.getActivityId() + "_" + param.getPrizeId();
        List<WinningRecordDO> winningRecordDOList = getWinningRecords(key);
        if (!CollectionUtils.isEmpty(winningRecordDOList)) {
            return convertToWinningRecordDTOList(winningRecordDOList);
        }
        // 如果redis不存在，查库
        winningRecordDOList = winningRecordMapper.selectByActivityIdOrPrizeId(
                param.getActivityId(), param.getPrizeId());
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("查询的中奖记录为空！param:{}",
                    JacksonUtil.writeValueAsString(param));
            return Arrays.asList();
        }
        // 回写记录到 redis
        cacheWinningRecords(key, winningRecordDOList, WINNING_RECORDS_TIMEOUT);
        return convertToWinningRecordDTOList(winningRecordDOList);
    }

    /**
     * 将中奖记录 DO 列表转换为 DTO 列表（奖品等级枚举转换）
     */
    private List<WinningRecordDTO> convertToWinningRecordDTOList(
            List<WinningRecordDO> winningRecordDOList) {
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            return Arrays.asList();
        }
        return winningRecordDOList.stream()
                .map(winningRecordDO -> {
                    WinningRecordDTO winningRecordDTO = new WinningRecordDTO();
                    winningRecordDTO.setWinnerId(winningRecordDO.getWinnerId());
                    winningRecordDTO.setWinnerName(winningRecordDO.getWinnerName());
                    winningRecordDTO.setPrizeName(winningRecordDO.getPrizeName());
                    winningRecordDTO.setPrizeTier(
                            ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()));
                    winningRecordDTO.setWinningTime(winningRecordDO.getWinningTime());
                    return winningRecordDTO;
                }).collect(Collectors.toList());
    }


    /**
     * 从缓存中删除中奖记录
     *
     * @param key 缓存 key（不含前缀）
     */
    private void deleteWinningRecords(String key) {
        try {
            if (redisUtil.hasKey(WINNING_RECORDS_PREFIX + key)) {
                redisUtil.del(WINNING_RECORDS_PREFIX + key);
            }
        } catch (Exception e) {
            logger.error("删除中奖记录缓存异常，key:{}", key);
        }
    }

    /**
     * 缓存中奖记录到 Redis
     *
     * @param key                  缓存 key（不含前缀）
     * @param winningRecordDOList  中奖记录列表
     * @param time                 过期时间
     */
    private void cacheWinningRecords(String key,
                                     List<WinningRecordDO> winningRecordDOList,
                                     Long time) {
        String str = "";
        try {
            if (!StringUtils.hasText(key)
                    || CollectionUtils.isEmpty(winningRecordDOList)) {
                logger.warn("要缓存的内容为空！key:{}, value:{}",
                        key, JacksonUtil.writeValueAsString(winningRecordDOList));
                return;
            }

            str = JacksonUtil.writeValueAsString(winningRecordDOList);
            redisUtil.set(WINNING_RECORDS_PREFIX + key,
                    str,
                    time);
        } catch (Exception e) {
            logger.error("缓存中奖记录异常！key:{}, value:{}", WINNING_RECORDS_PREFIX + key, str);
        }

    }

    /**
     * 从缓存中获取中奖记录
     *
     * @param key 缓存 key（不含前缀）
     * @return 中奖记录列表，缓存未命中或异常时返回空列表
     */
    private List<WinningRecordDO> getWinningRecords(String key) {
        try {
            if (!StringUtils.hasText(key)) {
                logger.warn("要从缓存中查询中奖记录的key为空！");
                return Arrays.asList();
            }
            String str = redisUtil.get(WINNING_RECORDS_PREFIX + key);
            if (!StringUtils.hasText(str)) {
                return Arrays.asList();
            }

            return JacksonUtil.readListValue(str, WinningRecordDO.class);
        } catch (Exception e) {
            logger.error("从缓存中查询中奖记录异常！key:{}", WINNING_RECORDS_PREFIX + key);
            return Arrays.asList();
        }
    }

}
