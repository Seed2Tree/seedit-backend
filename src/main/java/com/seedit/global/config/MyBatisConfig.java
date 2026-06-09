package com.seedit.global.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@MapperScan("com.seedit.domain.**.dao")
@Configuration
public class MyBatisConfig {
}
