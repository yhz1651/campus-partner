package com.example.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching //
@EnableScheduling // 开启定时任务
@MapperScan("com.example.springboot.mapper") // mybatis-plus扫描开启
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}




