package com.zwp.speeddating;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zwp.speeddating.mapper")
public class SpeedDatingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeedDatingApplication.class, args);
    }

}
