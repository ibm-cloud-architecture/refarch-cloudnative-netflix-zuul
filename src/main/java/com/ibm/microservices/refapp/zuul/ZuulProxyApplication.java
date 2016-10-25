package com.ibm.microservices.refapp.zuul;

import java.io.IOException;

import javax.annotation.Resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import com.ibm.microservices.refapp.zuul.filters.pre.JWTValidationPreFilter;

//import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(SecurityConfig.class)
public class ZuulProxyApplication {

    public static void main(String[] args) {
			SpringApplication.run(ZuulProxyApplication.class, args);
			System.out.println("Running "+ZuulProxyApplication.class+" via Spring Boot!");
    }
    
	@Bean
	public PropertySource<?> yamlPropertySourceLoader() throws IOException {
		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		PropertySource<?> applicationYamlPropertySource = loader.load(
				"application.yml", new ClassPathResource("application.yml"),"default");
		return applicationYamlPropertySource;
	}

	@Resource 
	private SecurityConfig securityConfig;
	
    
    @Bean
    public JWTValidationPreFilter authenticationFilter() {
    	return new JWTValidationPreFilter(securityConfig.getSharedSecret());
    }
    
}
