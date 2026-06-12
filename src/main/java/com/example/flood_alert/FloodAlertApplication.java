package com.example.flood_alert;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FloodAlertApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(FloodAlertApplication.class, args);
	}

}
