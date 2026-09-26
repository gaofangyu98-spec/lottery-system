package com.example.lotterysystem.service.mq;

import cn.hutool.core.date.DateUtil;
import com.example.lotterysystem.common.exception.ServiceException;
import com.example.lotterysystem.common.utils.JacksonUtil;
import com.example.lotterysystem.common.utils.MailUtil;
import com.example.lotterysystem.common.utils.SMSUtil;
import com.example.lotterysystem.controller.param.DrawPrizeParam;
import com.example.lotterysystem.dao.dataobject.ActivityPrizeDO;
import com.example.lotterysystem.dao.dataobject.WinningRecordDO;
import com.example.lotterysystem.dao.mapper.ActivityPrizeMapper;
import com.example.lotterysystem.dao.mapper.WinningRecordMapper;
import com.example.lotterysystem.service.DrawPrizeService;
import com.example.lotterysystem.service.activitystatus.ActivityStatusManager;
import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;
import com.example.lotterysystem.service.enums.ActivityPrizeStatusEnum;
import com.example.lotterysystem.service.enums.ActivityPrizeTiersEnum;
import com.example.lotterysystem.service.enums.ActivityStatusEnum;
import com.example.lotterysystem.service.enums.ActivityUserStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.QUEUE_NAME;

/**
 * 抽奖消息队列消费者
 * <p>
 * 接收 RabbitMQ 中的抽奖请求消息，执行完整的抽奖流程：
 * 1. 校验抽奖参数（活动/奖品是否有效、中奖人数是否匹配）
 * 2. 状态机流转（活动→奖品→人员 状态从 INIT/RUNNING 变为 COMPLETED）
 * 3. 保存中奖记录到数据库并写入 Redis 缓存
 * 4. 异步发送中奖通知（邮件 + 短信）
 * <p>
 * 异常处理：任何环节抛出异常时，调用 rollback 方法回滚已变更的状态和中奖记录，
 * 然后重新抛出异常触发 MQ 消息重试。
 */
@Component
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);
    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private MailUtil mailUtil;
    @Autowired
    private SMSUtil smsUtil;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;

    /**
     * 处理抽奖消息的主入口
     * <p>
     * 注意：可能存在前端重复提交相同请求的情况。若第一个请求已将活动/奖品状态
     * 扭转至 COMPLETED，第二个请求会在校验阶段被拦截；若状态扭转已完成但保存
     * 中奖记录失败，则需要通过 rollback 恢复状态。
     *
     * @param message MQ 消息体，包含 messageId 和 messageData（JSON 序列化的 DrawPrizeParam）
     */
    @RabbitHandler
    public void process(Map<String, String> message) throws Exception {
        logger.info("MQ成功接收到消息，message:{}",
                JacksonUtil.writeValueAsString(message));
        String paramString = message.get("messageData");
        DrawPrizeParam param = JacksonUtil.readValue(paramString, DrawPrizeParam.class);

        try {
            // 1、校验抽奖请求是否有效（活动是否完成、奖品是否完成、中奖人数是否匹配）
            if (!drawPrizeService.checkDrawPrizeParam(param)) {
                return;
            }

            // 2、状态扭转处理（责任链/策略模式：人员、奖品 → 活动）
            statusConvert(param);

            // 3、保存中奖者名单（写库 + 写缓存）
            List<WinningRecordDO> winningRecordDOList =
                    drawPrizeService.savewinnerRecords(param);

            // 4、通知中奖者（邮箱、短信）—— 异步线程池并发处理，不阻塞主流程
            syncExecute(winningRecordDOList);

        } catch (ServiceException e) {
            logger.error("处理 MQ 消息异常！{}:{}", e.getCode(), e.getMessage(), e);
            // 业务异常：回滚已变更的数据，然后抛出以触发 MQ 消息重试
            rollback(param);
            throw e;

        } catch (Exception e) {
            logger.error("处理 MQ 消息异常！", e);
            // 系统异常：回滚已变更的数据，然后抛出以触发 MQ 消息重试
            rollback(param);
            throw e;
        }
    }

    /**
     * 处理抽奖异常的回滚行为：恢复处理请求之前的库表状态
     * <p>
     * 分别判断状态和中奖记录是否需要回滚，避免不必要的数据库操作。
     *
     * @param param 抽奖请求参数
     */
    private void rollback(DrawPrizeParam param) {
        // 1、回滚状态：活动、奖品、人员
        if (!statusNeedRollback(param)) {
            return;
        }
        rollbackStatus(param);

        // 2、回滚中奖者名单
        if (!winnerNeedRollback(param)) {
            return;
        }
        rollbackWinner(param);
    }

    /**
     * 回滚中奖记录：删除奖品下的中奖者
     *
     * @param param 抽奖请求参数
     */
    private void rollbackWinner(DrawPrizeParam param) {
        drawPrizeService.deleteRecords(param.getActivityId(), param.getPrizeId());
    }

    /**
     * 判断中奖记录是否需要回滚（奖品下是否已存在中奖记录）
     */
    private boolean winnerNeedRollback(DrawPrizeParam param) {
        int count = winningRecordMapper.countByAPId(param.getActivityId(), param.getPrizeId());
        return count > 0;
    }

    /**
     * 恢复相关状态为 INIT/RUNNING
     * <p>
     * 通过 ActivityStatusManager 责任链统一回滚：奖品→INIT，人员→INIT，活动→RUNNING。
     *
     * @param param 抽奖请求参数
     */
    private void rollbackStatus(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.RUNNING);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.INIT);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.INIT);
        activityStatusManager.rollbackHandlerEvent(convertActivityStatusDTO);
    }

    /**
     * 判断状态是否需要回滚
     * <p>
     * 由于状态扭转在事务中保证一致性（奖品和人员要么都扭转、要么都没扭转），
     * 只需判断奖品状态是否已变为 COMPLETED，即可推断全部状态是否已扭转。
     * 不能通过活动状态判断，因为活动状态依赖所有奖品抽完才变更。
     */
    private boolean statusNeedRollback(DrawPrizeParam param) {
        ActivityPrizeDO activityPrizeDO =
                activityPrizeMapper.selectByAPId(param.getActivityId(), param.getPrizeId());
        return activityPrizeDO.getStatus()
                .equalsIgnoreCase(ActivityPrizeStatusEnum.COMPLETED.name());
    }

    /**
     * 并发处理抽奖后续流程（短信通知 + 邮件通知）
     * <p>
     * 通过线程池异步执行，避免通知逻辑阻塞主流程。后续可扩展为策略模式。
     *
     * @param winningRecordDOList 中奖记录列表
     */
    private void syncExecute(List<WinningRecordDO> winningRecordDOList) {
        threadPoolTaskExecutor.execute(() -> sendMessage(winningRecordDOList));
        threadPoolTaskExecutor.execute(() -> sendMail(winningRecordDOList));
    }

    /**
     * 向中奖者发送邮件通知
     *
     * @param winningRecordDOList 中奖记录列表
     */
    private void sendMail(List<WinningRecordDO> winningRecordDOList) {
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("中奖列表为空，不用发邮件！");
            return;
        }
        for (WinningRecordDO winningRecordDO : winningRecordDOList) {
            String context = "Hi，" + winningRecordDO.getWinnerName() + "。恭喜你在"
                    + winningRecordDO.getActivityName() + "活动中获得"
                    + ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()).getMessage()
                    + "：" + winningRecordDO.getPrizeName() + "。获奖时间为"
                    + DateUtil.formatTime(winningRecordDO.getWinningTime()) + "，请尽快领取您的奖励！";
            mailUtil.sendSampleMail(winningRecordDO.getWinnerEmail(),
                    "中奖通知", context);
        }
    }

    /**
     * 向中奖者发送短信通知
     *
     * @param winningRecordDOList 中奖记录列表
     */
    private void sendMessage(List<WinningRecordDO> winningRecordDOList) {
        if (CollectionUtils.isEmpty(winningRecordDOList)) {
            logger.info("中奖列表为空，不用发短信！");
            return;
        }
        for (WinningRecordDO winningRecordDO : winningRecordDOList) {
            Map<String, String> map = new HashMap<>();
            map.put("name", winningRecordDO.getWinnerName());
            map.put("activityName", winningRecordDO.getActivityName());
            map.put("prizeTiers", ActivityPrizeTiersEnum.forName(winningRecordDO.getPrizeTier()).getMessage());
            map.put("prizeName", winningRecordDO.getPrizeName());
            map.put("winningTime", DateUtil.formatTime(winningRecordDO.getWinningTime()));
            smsUtil.sendMessage("SMS_465985911",
                    winningRecordDO.getWinnerPhoneNumber().getValue(),
                    JacksonUtil.writeValueAsString(map));
        }
    }

    /**
     * 状态扭转：将活动、奖品、中奖人员状态统一流转为 COMPLETED
     * <p>
     * 通过 ActivityStatusManager 责任链模式执行，处理顺序由各 Operator 的 sequence 决定：
     * 先人员/奖品（sequence=1），后活动（sequence=2）。
     *
     * @param param 抽奖请求参数
     */
    private void statusConvert(DrawPrizeParam param) {
        ConvertActivityStatusDTO convertActivityStatusDTO = new ConvertActivityStatusDTO();
        convertActivityStatusDTO.setActivityId(param.getActivityId());
        convertActivityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.COMPLETED);
        convertActivityStatusDTO.setPrizeId(param.getPrizeId());
        convertActivityStatusDTO.setTargetPrizeStatus(ActivityPrizeStatusEnum.COMPLETED);
        convertActivityStatusDTO.setUserIds(
                param.getWinnerList().stream()
                        .map(DrawPrizeParam.Winner::getUserId)
                        .collect(Collectors.toList())
        );
        convertActivityStatusDTO.setTargetUserStatus(ActivityUserStatusEnum.COMPLETED);
        activityStatusManager.handlerEvent(convertActivityStatusDTO);
    }

}
