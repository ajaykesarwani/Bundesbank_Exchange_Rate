package com.bundesbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MainSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainSpringBootApplication.class, args);
	}

}
