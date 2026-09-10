package com.openmosque;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OpenMosqueApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenMosqueApplication.class, args);
    }
}
