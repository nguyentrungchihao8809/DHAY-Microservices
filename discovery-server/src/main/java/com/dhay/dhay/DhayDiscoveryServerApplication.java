package com.dhay.dhay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DhayDiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DhayDiscoveryServerApplication.class, args);
	}

}
