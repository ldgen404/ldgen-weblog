package com.ldgen.weblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.ldgen.weblog.mapper")
@EnableScheduling
public class LxWebLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(LxWebLogApplication.class, args);
    }

}
