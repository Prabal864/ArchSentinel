package com.archsentinel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ArchSentinelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArchSentinelApplication.class, args);
    }
}
