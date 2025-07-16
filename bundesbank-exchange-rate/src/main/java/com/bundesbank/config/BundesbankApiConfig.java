package com.bundesbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BundesbankApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}