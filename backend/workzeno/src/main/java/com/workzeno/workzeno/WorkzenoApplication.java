package com.workzeno.workzeno;

import com.workzeno.workzeno.identity.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WorkzenoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkzenoApplication.class, args);
	}

}

