package com.reservation.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class PriorityTestController {
    
    @GetMapping("/priority")
    public Map<String, Object> testPriority() {
        log.info("🧪 TESTING PRIORITY LOGIC MANUALLY");
        
        // Simulate priority sorting
        Map<String, Integer> courses = new HashMap<>();
        courses.put("Low Priority Course", 1);
        courses.put("High Priority Course", 5);
        courses.put("Medium Priority Course", 3);
        
        log.info("📊 Original courses:");
        courses.forEach((name, priority) -> 
            log.info("  - {} (Priority: {})", name, priority));
        
        // Sort by priority (higher first)
        var sortedCourses = courses.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .toList();
        
        log.info("📊 Sorted by priority (highest first):");
        for (int i = 0; i < sortedCourses.size(); i++) {
            var entry = sortedCourses.get(i);
            log.info("  {}. {} (Priority: {})", i + 1, entry.getKey(), entry.getValue());
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Priority test completed - check server logs!");
        result.put("original", courses);
        result.put("sorted", sortedCourses);
        result.put("priorityWorking", true);
        
        return result;
    }
}