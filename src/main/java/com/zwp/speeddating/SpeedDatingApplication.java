package com.zwp.speeddating;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.zwp.speeddating.mapper")
@EnableScheduling // 开启spring对定时任务的支持
public class SpeedDatingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeedDatingApplication.class, args);
    }

}
