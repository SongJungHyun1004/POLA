package com.jinjinjara.pola;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolaApplication {
	public static void main(String[] args) {
		SpringApplication.run(PolaApplication.class, args);
	}
}
