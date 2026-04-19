package com.example.memorymonitor;  // SAME package as main class

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Memory Monitor Backend is Running ✅";
    }
}