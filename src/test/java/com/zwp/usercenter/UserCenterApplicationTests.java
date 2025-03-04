package com.zwp.usercenter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SpringBootTest // 默认入口类与启动类名称一致，所以不用指定启动类或加@RunWith(SpringRunner.class)
class UserCenterApplicationTests {

    @Test
    void testDigest() {
        String newpassword = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(newpassword);
    }

    @Test
    void contextLoads() {
    }

}
