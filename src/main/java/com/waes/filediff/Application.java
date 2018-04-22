package com.waes.filediff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starts the application which provides means to check for 
 * the difference between 2 provided base64 encoded data.
 * 
 * @author Rodrigo Hackbarth
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
