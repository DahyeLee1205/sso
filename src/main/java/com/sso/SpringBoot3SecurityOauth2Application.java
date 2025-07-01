package com.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ConfigurationPropertiesScan("com.sso.oauth.config.properties")
public class SpringBoot3SecurityOauth2Application {
    public static void main(String[] args) {
        SpringApplication.run(SpringBoot3SecurityOauth2Application.class, args);
    }
}