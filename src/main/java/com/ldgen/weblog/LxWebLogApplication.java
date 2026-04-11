package com.ldgen.weblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ldgen.weblog.mapper")
public class LxWebLogApplication {

    public static void main(String[] args) {
        SpringApplication.run(LxWebLogApplication.class, args);
    }

}
