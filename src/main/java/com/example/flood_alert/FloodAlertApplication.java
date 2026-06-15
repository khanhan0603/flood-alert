package com.example.flood_alert;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class FloodAlertApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(
				TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(FloodAlertApplication.class, args);
	}

}
