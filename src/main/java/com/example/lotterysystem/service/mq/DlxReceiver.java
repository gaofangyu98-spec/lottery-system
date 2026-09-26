package com.example.lotterysystem.service.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.example.lotterysystem.common.config.DirectRabbitConfig.*;

/**
 * 死信队列（Dead Letter Exchange）消费者
 * <p>
 * 当普通队列中的消息处理失败达到最大重试次数后，消息会被投递到死信队列。
 * 当前实现为演示用途：直接将死信消息重新投递回普通队列进行重试。
 * <p>
 * 生产环境正确流程：将异常消息持久化到数据库 → 人工/定时任务排查问题 → 重新投递。
 */
@Component
@RabbitListener(queues = DLX_QUEUE_NAME)
public class DlxReceiver {

    private static final Logger logger = LoggerFactory.getLogger(DlxReceiver.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 处理死信消息：将异常消息重新投递回普通交换机
     *
     * @param message 死信消息体
     */
    @RabbitHandler
    public void process(Map<String, String> message) {
        // 死信队列的处理方法
        logger.info("开始处理异常消息！");
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING, message);
        // 该流程是有问题的，在这里只是为了演示处理过程中发生异常：消息堆积-》处理异常-》消息重发
        // 正确的流程（扩展）：
        // 1、接收到异常消息，可以将异常消息存放到数据库表中
        // 2、存放后，当前异常消息消费完成，死信队列消息处理完成，但异常消息被我们持久化存储到表中了
        // 3、解决异常
        // 4、完成脚本任务，判断异常消息表中是否存在数据，如果存在，表示有消息未完成，此时处理消息
        // 5、处理消息：将消息发送给普通队列进行处理
    }
}
