package com.reservation.service;

import com.reservation.repository.EventRepository;
import com.reservation.repository.RoomRepository;
import com.reservation.repository.CourseRepository;
import com.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatbotService {
    
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SchedulingAnalyticsService analyticsService;
    
    @Value("${ai.service.type:ollama}")
    private String aiServiceType;
    
    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaApiUrl;
    
    @Value("${ai.model:llama3.2}")
    private String aiModel;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String processUserMessage(String userMessage) {
        try {
            log.info("🤖 Processing user message: {}", userMessage);
            
            // Get current system context (real-time data)
            String systemContext = buildSystemContext();
            
            // Create AI prompt with context
            String aiPrompt = buildAIPrompt(userMessage, systemContext);
            
            // Call local AI service based on configuration
            String aiResponse;
            switch (aiServiceType.toLowerCase()) {
                case "ollama":
                    aiResponse = callOllamaAPI(aiPrompt);
                    break;
                case "huggingface":
                    aiResponse = callHuggingFaceLocal(aiPrompt);
                    break;
                case "openai":
                    aiResponse = callOpenAI(aiPrompt);
                    break;
                default:
                    aiResponse = callOllamaAPI(aiPrompt);
            }
            
            log.info("🤖 AI Response generated successfully");
            return aiResponse;
            
        } catch (Exception e) {
            log.error("❌ Error processing chatbot message: {}", e.getMessage(), e);
            return generateFallbackResponse(userMessage);
        }
    }
    
    private String buildSystemContext() {
        try {
            // Get real-time data from YOUR specific reservation system
            long totalEvents = eventRepository.count();
            long totalRooms = roomRepository.count();
            long totalCourses = courseRepository.count();
            long totalUsers = userRepository.count();
            
            // Get today's analytics from YOUR system
            Map<String, Object> todayAnalytics = analyticsService.getComprehensiveAnalytics("today");
            
            StringBuilder context = new StringBuilder();
            context.append("==== YOUR UNIVERSITY ROOM RESERVATION SYSTEM ====\n");
            context.append("Project: Academic Room Scheduling & Management Platform\n");
            context.append("Date: ").append(LocalDate.now().format(DateTimeFormatter.ISO_DATE)).append("\n\n");
            
            context.append("CURRENT SYSTEM STATUS:\n");
            context.append("📚 Total Scheduled Events: ").append(totalEvents).append(" events\n");
            context.append("🏢 Available Rooms: ").append(totalRooms).append(" rooms\n");
            context.append("📖 Active Courses: ").append(totalCourses).append(" courses\n");
            context.append("👥 Registered Users: ").append(totalUsers).append(" users (teachers/admins)\n\n");
            
            context.append("TODAY'S ANALYTICS:\n");
            if (todayAnalytics != null) {
                context.append("📊 Real-time data: ").append(todayAnalytics).append("\n");
            } else {
                context.append("📊 No events scheduled for today\n");
            }
            
            context.append("\nSYSTEM CAPABILITIES:\n");
            context.append("✅ Room booking and scheduling\n");
            context.append("✅ Course-room assignments\n");
            context.append("✅ Conflict detection and resolution\n");
            context.append("✅ Usage analytics and reporting\n");
            context.append("✅ User management for teachers/admins\n");
            context.append("✅ Real-time availability checking\n\n");
            
            context.append("IMPORTANT: This is a live academic reservation system with real data.\n");
            context.append("All numbers and statistics are current and accurate.\n");
            
            return context.toString();
        } catch (Exception e) {
            log.error("Error building system context", e);
            return "Your university reservation system data is temporarily unavailable.";
        }
    }
    
    private String buildAIPrompt(String userMessage, String systemContext) {
        return String.format("""
            You are the AI assistant for THIS SPECIFIC university room reservation system.
            You have access to LIVE DATA from this academic scheduling platform.
            
            SYSTEM YOU'RE MANAGING:
            %s
            
            CRITICAL INSTRUCTIONS:
            - Answer questions about THIS SPECIFIC reservation system (not general information)
            - Use the EXACT NUMBERS from the system data provided above
            - Reference the specific rooms, courses, and events in THIS system
            - When asked "How many rooms?", use the exact count from "Available Rooms"
            - When asked about bookings, use the exact "Total Scheduled Events" number
            - When asked about courses, use the exact "Active Courses" count
            - When asked about users, use the exact "Registered Users" number
            - Be specific: "In YOUR system, you have X rooms..." not "Generally, systems have..."
            - Provide actionable insights based on THIS system's actual data
            - If data shows 0, explain that this might be a new system or no data is loaded yet
            
            RESPONSE STYLE:
            - Professional and academic tone
            - Specific to this university reservation system
            - Include exact numbers from the data above
            - Suggest relevant actions for THIS system
            
            USER QUESTION: %s
            
            Provide a precise, data-driven response about THIS SPECIFIC system:
            """, systemContext, userMessage);
    }
    
    private String callOllamaAPI(String prompt) {
        try {
            String url = ollamaApiUrl + "/api/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiModel);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                "temperature", 0.7,
                "top_p", 0.9,
                "max_tokens", 500
            ));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            log.info("🤖 Calling Ollama API at: {}", url);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("response")) {
                    String aiResponse = (String) responseBody.get("response");
                    return aiResponse.trim();
                }
            }
            
            return "I couldn't generate a response right now. Please make sure Ollama is running.";
            
        } catch (Exception e) {
            log.error("Error calling Ollama API", e);
            return "🔌 Ollama AI is not available. Please install and run Ollama, or I can help you set it up!";
        }
    }
    
    private String callHuggingFaceLocal(String prompt) {
        // Placeholder for local Hugging Face implementation
        return "HuggingFace local model not implemented yet. Using Ollama instead.";
    }
    
    private String generateFallbackResponse(String userMessage) {
        // Intelligent fallback based on keywords and system data
        try {
            String lowerMessage = userMessage.toLowerCase();
            
            if (lowerMessage.contains("room") || lowerMessage.contains("how many")) {
                if (lowerMessage.contains("total") || lowerMessage.contains("how many room")) {
                    long roomCount = roomRepository.count();
                    return String.format("📊 **System Info**: We currently have **%d rooms** in our system.", roomCount);
                }
            }
            
            if (lowerMessage.contains("event") || lowerMessage.contains("booking")) {
                long eventCount = eventRepository.count();
                return String.format("📅 **Current Status**: There are **%d total events** scheduled in our system.", eventCount);
            }
            
            if (lowerMessage.contains("course")) {
                long courseCount = courseRepository.count();
                return String.format("📚 **Academic Info**: We have **%d courses** available for scheduling.", courseCount);
            }
            
            if (lowerMessage.contains("user") || lowerMessage.contains("teacher")) {
                long userCount = userRepository.count();
                return String.format("👥 **User Stats**: There are **%d users** registered in our system.", userCount);
            }
            
            if (lowerMessage.contains("analytic") || lowerMessage.contains("report") || lowerMessage.contains("stat")) {
                return "📊 **Analytics Available**: I can help you with room utilization, scheduling patterns, and usage reports. Try asking about specific metrics!";
            }
            
            // Default intelligent response
            return String.format("""
                🤖 **AI Assistant Ready!**
                
                I'm here to help with your reservation system. Here's what I can tell you:
                
                📊 **Current System Overview:**
                • %d total events scheduled
                • %d rooms available
                • %d courses offered  
                • %d registered users
                
                💡 **Ask me about:**
                • Room usage and availability
                • Scheduling analytics and reports
                • Popular time slots and patterns
                • System utilization insights
                
                *Note: For advanced AI responses, you can install Ollama (free) for smarter conversations!*
                """, 
                eventRepository.count(),
                roomRepository.count(), 
                courseRepository.count(),
                userRepository.count());
                
        } catch (Exception e) {
            log.error("Error generating fallback response", e);
            return "🤖 I'm your reservation system assistant! Ask me about rooms, bookings, analytics, or system statistics.";
        }
    }
    
    @Value("${openai.api.key:}")
    private String openaiApiKey;
    
    private String callOpenAI(String prompt) {
        if (openaiApiKey == null || openaiApiKey.isEmpty()) {
            return "⚠️ AI service not configured. Please add your OpenAI API key to application.properties (openai.api.key=your-key)";
        }
        
        try {
            String url = "https://api.openai.com/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("max_tokens", 500);
            requestBody.put("temperature", 0.7);
            
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            messages.add(message);
            requestBody.put("messages", messages);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.POST, request, 
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        @SuppressWarnings("unchecked")
                        Map<String, String> messageContent = (Map<String, String>) firstChoice.get("message");
                        if (messageContent != null) {
                            return messageContent.get("content").trim();
                        }
                    }
                }
            }
            
            return "I couldn't generate a response right now. Please try again.";
            
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            return "I'm having trouble connecting to the AI service. Please try again later.";
        }
    }
    
    public List<String> getSuggestedQuestions() {
        return Arrays.asList(
            "How many rooms are booked today?",
            "Which rooms are most popular this week?",
            "Show me scheduling conflicts for tomorrow",
            "What's the occupancy rate for our rooms?",
            "Which teachers have the most classes scheduled?",
            "Generate a summary report for this month",
            "What are the peak usage hours?",
            "Which courses need more room time?"
        );
    }
}
