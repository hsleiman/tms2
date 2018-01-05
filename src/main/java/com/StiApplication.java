package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySources({
    @PropertySource( value = "classpath:application.properties" )
})
@SpringBootApplication
@EnableJpaRepositories
@EnableAutoConfiguration
@EnableTransactionManagement
public class StiApplication {

	public static void main(String[] args) {
		SpringApplication.run(StiApplication.class, args);
	}
}
