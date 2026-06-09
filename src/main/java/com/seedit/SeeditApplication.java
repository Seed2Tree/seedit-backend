package com.seedit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.seedit.feature.**.repository")
public class SeeditApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeeditApplication.class, args);
	}

}
