package com.example.lotterysystem;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 日志输出测试：对比 System.out 与 slf4j logger 的输出。
 *
 * @author: yibo
 */
@SpringBootTest
public class LogTest {
    private final static Logger logger = LoggerFactory.getLogger(LogTest.class);

    @Test
    void logTest() {
        System.out.println("hello world");
        logger.info("hello world");
    }

}
