package com.veda.central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class VedaAuthApplicationService {
    public static void main(String[] args) {
        SpringApplication.run(VedaAuthApplicationService.class, args);
    }
}
