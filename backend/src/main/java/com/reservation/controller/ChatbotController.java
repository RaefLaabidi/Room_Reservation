package com.reservation.controller;

import com.reservation.service.AIChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {
    
    private final AIChatbotService chatbotService;
    
    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            
            if (userMessage == null || userMessage.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Message cannot be empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            log.info("🤖 Received chatbot message: {}", userMessage);
            
            // Process message with AI
            String aiResponse = chatbotService.processUserMessage(userMessage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", aiResponse);
            response.put("timestamp", System.currentTimeMillis());
            response.put("type", "ai");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error in chatbot controller: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Sorry, I encountered an error. Please try again.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions() {
        try {
            List<String> suggestions = chatbotService.getSuggestedQuestions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("suggestions", suggestions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error getting suggestions: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Could not load suggestions"));
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getChatbotStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "online");
        status.put("service", "AI Admin Assistant");
        status.put("capabilities", List.of(
            "Room analytics and insights",
            "Scheduling statistics", 
            "Resource utilization reports",
            "Conflict analysis",
            "Usage pattern analysis"
        ));
        
        return ResponseEntity.ok(status);
    }
}
