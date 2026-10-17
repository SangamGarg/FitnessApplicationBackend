package com.fitnessapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class FitnessAppBackendApplication {

    public static void main(String[] args) {
        SpringApplication. run(FitnessAppBackendApplication.class, args);
        log.info("Server started");
    }
}