package com.example.lotterysystem;

import com.example.lotterysystem.service.UserService;
import com.example.lotterysystem.service.dto.UserDTO;
import com.example.lotterysystem.service.enums.UserIdentityEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 用户服务测试：按身份查询用户列表。
 *
 * @author: yibo
 */
@SpringBootTest
public class UserTest {

    @Autowired
    private UserService userService;

    @Test
    void findBaseUserList() {

        List<UserDTO> userDTOList = userService.findUserInfo(UserIdentityEnum.ADMIN);
        for (UserDTO userDTO : userDTOList) {
            System.out.println(userDTO.getUserId() + " " + userDTO.getUserName() + " " + userDTO.getIdentity().name());
        }
    }

}
