package com.appdynamics.cloud.prometheus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppDPrometheusApplication {

	public static void main(String[] args) {
		
		//SpringApplication.run(AppDPrometheusApplication.class, args);
	    SpringApplication springApplication = new SpringApplication(AppDPrometheusApplication.class);
	    springApplication.addListeners(new AppDPrometheusAppListener());
	    springApplication.run(args);
	}

}
