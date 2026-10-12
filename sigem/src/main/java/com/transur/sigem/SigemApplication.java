package com.transur.sigem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SigemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SigemApplication.class, args);
	}

}
