package com.something;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.something.dao.mapper")
@SpringBootApplication
public class AdminApplication {
    public static void main(java.lang.String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
