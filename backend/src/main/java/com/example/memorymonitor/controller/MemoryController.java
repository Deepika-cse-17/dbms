package com.example.memorymonitor.controller;

import com.example.memorymonitor.service.MemoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = memoryService.collectMetrics();
        return ResponseEntity.ok(status);
    }

    @GetMapping("/logs/recent")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getRecentLogs() {
        return ResponseEntity.ok(memoryService.getRecentLogs());
    }

    @GetMapping("/logs/alerts")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> getAlertLogs() {
        return ResponseEntity.ok(memoryService.getAlertLogs());
    }
}
