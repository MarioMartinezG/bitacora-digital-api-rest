package com.diginexa.bitacora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BitacoraDigitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BitacoraDigitalApplication.class, args);
	}

}
