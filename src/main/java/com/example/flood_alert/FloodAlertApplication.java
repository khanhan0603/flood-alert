package com.example.flood_alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FloodAlertApplication {

	public static void main(String[] args) {
		SpringApplication.run(FloodAlertApplication.class, args);
	}

}
