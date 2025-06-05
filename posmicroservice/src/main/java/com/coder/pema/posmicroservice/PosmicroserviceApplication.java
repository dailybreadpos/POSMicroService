package com.coder.pema.posmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PosmicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PosmicroserviceApplication.class, args);
	}

}
