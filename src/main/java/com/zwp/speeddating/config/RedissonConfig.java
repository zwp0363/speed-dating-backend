package com.zwp.speeddating.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson(java操作 Redis 的客户端) 配置
 */
@Configuration
// Spring Boot 的 @ConfigurationProperties 注解，指定读取配置文件中 spring.redis 前缀的配置
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    // Spring Boot 会尝试将 application.yml 中 spring.redis.host 的值注入到这个属性中
    private String host;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient redissonClient() {
        // 1.创建配置
        Config config = new Config();
        // redis://127.0.0.1:6379
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(password);
        // 2.创建实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
