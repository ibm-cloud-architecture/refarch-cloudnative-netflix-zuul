package com.ibm.microservices.refapp.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

import com.ibm.microservices.refapp.zuul.filters.pre.JWTValidationPreFilter;

//import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
public class ZuulProxyApplication {

    public static void main(String[] args) {
			SpringApplication.run(ZuulProxyApplication.class, args);
			System.out.println("Running "+ZuulProxyApplication.class+" via Spring Boot!");
    }
    
    
    @Bean
    public JWTValidationPreFilter authenticationFilter() {
    	return new JWTValidationPreFilter();
    }

}
