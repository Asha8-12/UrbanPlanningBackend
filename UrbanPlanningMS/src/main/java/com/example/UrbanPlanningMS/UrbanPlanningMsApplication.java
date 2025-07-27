package com.example.UrbanPlanningMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class UrbanPlanningMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrbanPlanningMsApplication.class, args);

	}

}
