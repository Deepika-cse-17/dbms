package com.example.memorymonitor.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemoryService {

    private final JdbcTemplate jdbcTemplate;
    private final JavaMailSender mailSender;
    private final List<String> cache = new ArrayList<>();
    private static final String SENDER_EMAIL = "yourdesignathon@gmail.com";
    
    @Value("${memory.alert.email:yourdesignathon@gmail.com}")
    private String alertEmail;

    public MemoryService(JdbcTemplate jdbcTemplate, JavaMailSender mailSender) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
    }

    public Map<String, Object> collectMetrics() {
        Map<String, Object> memoryStatus = getMemoryStatus();
        double percent = (double) memoryStatus.get("percent");
        double prediction = predictMemory(percent);
        List<String> currentCache = manageCache("Sample_Data", percent);
        String action = percent > 80 ? "Cache Cleared" : "Cache Active";
        String alertStatus = "NORMAL";

        insertLog((double) memoryStatus.get("used"), (double) memoryStatus.get("available"), percent, action, alertStatus, false);

        Map<String, Object> response = new HashMap<>();
        response.put("memory", memoryStatus);
        response.put("prediction", prediction);
        response.put("cache", currentCache);
        response.put("action", action);

        return response;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void sendStartupMemoryReport() {
        Map<String, Object> memoryStatus = getMemoryStatus();
        double used = (double) memoryStatus.get("used");
        double available = (double) memoryStatus.get("available");
        double percent = (double) memoryStatus.get("percent");

        sendPeriodicEmail(alertEmail, used, available, percent);
    }

    public List<Map<String, Object>> getRecentLogs() {
        return jdbcTemplate.query(
                "SELECT id, used, available, percent, action, alert_status, alert_sent, created_at FROM memory_logs ORDER BY created_at DESC LIMIT 5",
                (rs, rowNum) -> mapLogRow(rs)
        );
    }

    public List<Map<String, Object>> getAlertLogs() {
        return jdbcTemplate.query(
                "SELECT id, used, available, percent, action, alert_status, alert_sent, created_at FROM memory_logs WHERE alert_status = 'HIGH' ORDER BY created_at DESC",
                (rs, rowNum) -> mapLogRow(rs)
        );
    }

    private Map<String, Object> getMemoryStatus() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalBytes = osBean.getTotalPhysicalMemorySize();
        long freeBytes = osBean.getFreePhysicalMemorySize();
        long usedBytes = totalBytes - freeBytes;

        double totalGb = roundToTwo(totalBytes / (1024.0 * 1024 * 1024));
        double usedGb = roundToTwo(usedBytes / (1024.0 * 1024 * 1024));
        double availableGb = roundToTwo(freeBytes / (1024.0 * 1024 * 1024));
        double percent = roundToTwo((usedBytes / (double) totalBytes) * 100);

        Map<String, Object> memory = new HashMap<>();
        memory.put("total", totalGb);
        memory.put("used", usedGb);
        memory.put("available", availableGb);
        memory.put("percent", percent);
        return memory;
    }

    private double predictMemory(double currentPercent) {
        double predicted = currentPercent + 5;
        if (predicted > 100) {
            predicted = 100;
        }
        return roundToTwo(predicted);
    }

    private List<String> manageCache(String data, double percent) {
        if (percent > 80) {
            cache.clear();
        } else {
            cache.add(data);
        }
        return new ArrayList<>(cache);
    }

    private void insertLog(double used, double available, double percent, String action, String alertStatus, boolean alertSent) {
        jdbcTemplate.update(
                "INSERT INTO memory_logs (used, available, percent, action, alert_status, alert_sent) VALUES (?, ?, ?, ?, ?, ?)",
                used, available, percent, action, alertStatus, alertSent
        );
    }

    private Map<String, Object> mapLogRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Map<String, Object> row = new HashMap<>();
        row.put("id", rs.getLong("id"));
        row.put("used", rs.getDouble("used"));
        row.put("available", rs.getDouble("available"));
        row.put("percent", rs.getDouble("percent"));
        row.put("action", rs.getString("action"));
        row.put("alertStatus", rs.getString("alert_status"));
        row.put("alertSent", rs.getBoolean("alert_sent"));
        row.put("createdAt", rs.getTimestamp("created_at").toString());
        return row;
    }

    private void sendPeriodicEmail(String recipient, double used, double available, double percent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(SENDER_EMAIL, "RAM Monitor System");
            helper.setTo(recipient);
            helper.setSubject("Periodic Memory Usage Report");
            
            String emailBody = String.format(
                "Memory Usage Report:\n\n" +
                "Used Memory: %.2f GB\n" +
                "Available Memory: %.2f GB\n" +
                "Memory Usage: %.2f%%\n\n" +
                "This is an automated periodic report sent every minute.",
                used, available, percent
            );
            
            helper.setText(emailBody, false);
            mailSender.send(message);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double roundToTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
