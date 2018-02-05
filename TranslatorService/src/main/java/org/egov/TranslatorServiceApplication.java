package org.egov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@SpringBootApplication
@EnableZuulProxy
public class TranslatorServiceApplication {
	

	public static void main(String[] args) {
		SpringApplication.run(TranslatorServiceApplication.class, args);
	}
	
	
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
	    return new ObjectMapper();
	}
	
	@Bean("YamlReader")
	public ObjectMapper yamlObjectMapper() {
	    return new ObjectMapper(new YAMLFactory());
	}
}
