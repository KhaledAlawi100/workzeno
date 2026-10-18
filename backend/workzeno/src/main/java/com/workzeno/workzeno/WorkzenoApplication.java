package com.workzeno.workzeno;

import com.workzeno.workzeno.identity.security.JwtProperties;
import com.workzeno.workzeno.identity.security.RefreshTokenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, RefreshTokenProperties.class})
public class WorkzenoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkzenoApplication.class, args);
	}

}

