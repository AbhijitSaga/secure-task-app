package com.secure_task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;

/*@SpringBootApplication(exclude = {
		OAuth2ClientAutoConfiguration.class
})*/
@SpringBootApplication
public class SecureTaskAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureTaskAppApplication.class, args);
	}

}
