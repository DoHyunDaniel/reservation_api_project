package com.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
@SpringBootApplication
public class ReservationApiProjectApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load(); // .env 자동 로드
		System.setProperty("AWS_ACCESS_KEY", dotenv.get("AWS_ACCESS_KEY"));
		System.setProperty("AWS_SECRET_KEY", dotenv.get("AWS_SECRET_KEY"));
		
		SpringApplication.run(ReservationApiProjectApplication.class, args);
	}

}
