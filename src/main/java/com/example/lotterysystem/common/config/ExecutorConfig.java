package com.example.lotterysystem.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置类
 * <p>开启 {@link EnableAsync} 后，供 {@code @Async} 异步方法使用。
 * 核心/最大线程数、队列容量、线程名前缀均通过配置文件注入。</p>
 *
 * @author: yibo
 */
@Configuration
@EnableAsync
public class ExecutorConfig {
    /** 核心线程数 */
    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize;
    /** 最大线程数 */
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize;
    /** 队列容量：核心线程都忙时，新任务先进入队列排队 */
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity;
    /** 线程名前缀，便于日志排查 */
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix;


    /**
     * 构建异步任务线程池
     */
    @Bean(name = "asyncServiceExecutor")
    public ThreadPoolTaskExecutor asyncServiceExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new
                ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        // 空闲线程超过该秒数后回收
        threadPoolTaskExecutor.setKeepAliveSeconds(3);
        threadPoolTaskExecutor.setThreadNamePrefix(namePrefix);
        // 拒绝策略：当线程数达到 max 且队列满时，直接抛出 RejectedExecutionException
        threadPoolTaskExecutor.setRejectedExecutionHandler(new
                ThreadPoolExecutor.AbortPolicy());
        // 初始化线程池
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}