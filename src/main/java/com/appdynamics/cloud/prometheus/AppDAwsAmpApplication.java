package com.appdynamics.cloud.prometheus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppDAwsAmpApplication {

	public static void main(String[] args) {
		
		//SpringApplication.run(AppDAwsAmpApplication.class, args);
	    SpringApplication springApplication = new SpringApplication(AppDAwsAmpApplication.class);
	    springApplication.addListeners(new AppDAwsAmpAppListener());
	    springApplication.run(args);
	}

}
