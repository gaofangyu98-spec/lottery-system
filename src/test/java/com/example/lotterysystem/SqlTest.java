package com.example.lotterysystem;

import com.example.lotterysystem.dao.dataobject.Encrypt;
import com.example.lotterysystem.dao.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Mapper SQL 测试：按邮箱/手机号统计用户数量。
 *
 * @author: yibo
 */
@SpringBootTest
public class SqlTest {

    @Autowired
    private UserMapper userMapper;


    @Test
    void mailCount() {
        int count = userMapper.countByMail("123@qq.com");
        System.out.println("mailCount=" + count);
    }

    @Test
    void phoneCount() {
        int count = userMapper.countByPhone(new Encrypt("13111111111"));
        System.out.println("phoneCount=" + count);
    }

}
