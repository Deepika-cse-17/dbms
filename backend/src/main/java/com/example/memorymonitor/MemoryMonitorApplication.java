package com.example.memorymonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MemoryMonitorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemoryMonitorApplication.class, args);
    }
}
