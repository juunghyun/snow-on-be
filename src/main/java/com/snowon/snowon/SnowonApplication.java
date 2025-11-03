package com.snowon.snowon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@SpringBootApplication
public class SnowonApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnowonApplication.class, args);
	}

}
