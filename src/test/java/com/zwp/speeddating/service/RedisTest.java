package com.zwp.speeddating.service;

import com.zwp.speeddating.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();// 字符串类型
        // 增
        valueOperations.set("zwpString", "dog");
        valueOperations.set("zwpInt", 1);
        valueOperations.set("zwpDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("zwp");
        valueOperations.set("zwpUser", user);
        // 查
        Object zwp = valueOperations.get("zwpString");
        Assertions.assertTrue("dog".equals((String) zwp));
        zwp = valueOperations.get("zwpInt");
        Assertions.assertTrue(1 == (Integer) zwp);
        zwp = valueOperations.get("zwpDouble");
        Assertions.assertTrue(2.0 == (Double) zwp);
        zwp = valueOperations.get("zwpUser");
        System.out.println(zwp);
        // 改
        valueOperations.set("zwpString", "dog1");
        // 删
        redisTemplate.delete("zwpString");
    }
}
