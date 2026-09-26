package com.example.lotterysystem.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 直连交换机配置
 * <p>声明一套正常的 Direct 交换机/队列/路由键，以及配套的死信交换机（DLX）。
 * 正常队列在消息被拒绝或过期时会路由到死信队列，便于排查异常消息。</p>
 *
 * @author: yibo
 */
@Configuration
public class DirectRabbitConfig {
    /** 普通队列名 */
    public static final String QUEUE_NAME = "DirectQueue";
    /** 普通交换机名 */
    public static final String EXCHANGE_NAME = "DirectExchange";
    /** 普通路由键 */
    public static final String ROUTING = "DirectRouting";

    /** 死信队列名 */
    public static final String DLX_QUEUE_NAME = "DlxDirectQueue";
    /** 死信交换机名 */
    public static final String DLX_EXCHANGE_NAME = "DlxDirectExchange";
    /** 死信路由键 */
    public static final String DLX_ROUTING = "DlxDirectRouting";

    /**
     * 队列 起名：DirectQueue
     *
     * @return
     */
    @Bean
    public Queue directQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。
        //   return new Queue("DirectQueue",true,true,false);

        // 普通队列绑定死信交换机
        return QueueBuilder.durable(QUEUE_NAME)
                .deadLetterExchange(DLX_EXCHANGE_NAME)
                .deadLetterRoutingKey(DLX_ROUTING).build();
    }

    /**
     * Direct交换机 起名：DirectExchange
     *
     * @return
     */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定  将队列和交换机绑定, 并设置用于匹配键：DirectRouting
     *
     * @return
     */
    @Bean
    Binding bindingDirect() {
        return BindingBuilder.bind(directQueue())
                .to(directExchange())
                .with(ROUTING);
    }

    /**
     * 死信队列
     *
     * @return
     */
    @Bean
    public Queue dlxQueue() {
        // durable:是否持久化,默认是false,持久化队列：会被存储在磁盘上，当消息代理重启时仍然存在，暂存队列：当前连接有效
        // exclusive:默认也是false，只能被当前创建的连接使用，而且当连接关闭后队列即被删除。此参考优先级高于durable
        // autoDelete:是否自动删除，当没有生产者或者消费者使用此队列，该队列会自动删除。

        // 一般设置一下队列的持久化就好,其余两个就是默认false
        return new Queue(DLX_QUEUE_NAME,true);
    }

    /**
     * 死信交换机
     *
     * @return
     */
    @Bean
    DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE_NAME,true,false);
    }

    /**
     * 绑定死信队列与交换机
     *
     * @return
     */
    @Bean
    Binding bindingDlx() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(DLX_ROUTING);
    }


    /**
     * JSON 消息转换器，使 RabbitMQ 消息以 JSON 序列化传输
     */
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
