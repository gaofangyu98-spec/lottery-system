package com.example.lotterysystem;

import com.example.lotterysystem.common.utils.SMSUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 短信发送测试。
 *
 * @author: yibo
 */
@SpringBootTest
public class SMSTest {

    @Autowired
    private SMSUtil smsUtil;

    @Test
    void smsTest() {
        smsUtil.sendMessage(
                "SMS_465324787",
                "15129270506",
                "{\"code\":\"1234\"}");
        // {"code":"1234"}
    }

}
