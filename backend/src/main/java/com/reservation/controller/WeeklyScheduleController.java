package com.reservation.controller;

import com.reservation.service.EnhancedWeeklyScheduleService;
import com.reservation.service.FastProfessionalScheduler;
import com.reservation.service.SchedulingAnalyticsService;
import com.reservation.repository.EventRepository;
import com.reservation.repository.RoomRepository;
import com.reservation.repository.CourseRepository;
import com.reservation.model.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weekly-schedule")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class WeeklyScheduleController {

    private final EnhancedWeeklyScheduleService enhancedScheduleService;
    private final FastProfessionalScheduler fastProfessionalScheduler;
    private final SchedulingAnalyticsService analyticsService;
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final CourseRepository courseRepository;

    @PostMapping("/generate")
    public ResponseEntity<?> generateWeeklySchedule(@RequestBody List<Long> courseIds) {
        try {
            log.info("🔄 Generating enhanced weekly schedule for {} courses", courseIds.size());
            
            // Create WeeklyScheduleRequest from courseIds
            EnhancedWeeklyScheduleService.WeeklyScheduleRequest request = new EnhancedWeeklyScheduleService.WeeklyScheduleRequest();
            request.setWeekStartDate(LocalDate.now().with(java.time.DayOfWeek.MONDAY)); // Start from current Monday
            
            List<EnhancedWeeklyScheduleService.CourseScheduleRequest> courseRequests = courseIds.stream()
                .map(courseId -> {
                    EnhancedWeeklyScheduleService.CourseScheduleRequest courseRequest = new EnhancedWeeklyScheduleService.CourseScheduleRequest();
                    courseRequest.setCourseId(courseId);
                    courseRequest.setPriority(5); // Default priority
                    courseRequest.setStudentCount(30); // Default student count
                    return courseRequest;
                })
                .collect(java.util.stream.Collectors.toList());
            
            request.setCourses(courseRequests);
            
            var schedule = enhancedScheduleService.createWeeklySchedule(request);
            
            log.info("✅ Enhanced schedule generated successfully");
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("❌ Enhanced schedule generation failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Schedule generation failed: " + e.getMessage());
        }
    }

    @PostMapping("/create-professional")
    public ResponseEntity<?> createProfessionalSchedule(@RequestBody List<Long> courseIds) {
        try {
            log.info("🚀 FAST PROFESSIONAL SCHEDULING - {} courses", courseIds.size());
            
            FastProfessionalScheduler.ProfessionalScheduleResult result = 
                fastProfessionalScheduler.scheduleCoursesOptimally(courseIds);
            
            log.info("✅ Fast scheduling complete: {}/{} courses scheduled", 
                    result.getScheduledEvents().size(), courseIds.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Fast professional scheduling failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Fast scheduling failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/analytics/test")
    @Transactional(readOnly = true)
    public ResponseEntity<?> testAnalytics() {
        try {
            log.info("🧪 Testing analytics endpoint - Request received");
            
            // Test database connectivity
            long eventCount = eventRepository.count();
            long roomCount = roomRepository.count();
            long courseCount = courseRepository.count();
            
            Map<String, Object> testResponse = new HashMap<>();
            testResponse.put("status", "success");
            testResponse.put("message", "Analytics endpoint is working");
            testResponse.put("timestamp", System.currentTimeMillis());
            testResponse.put("server", "ReservationBackend");
            testResponse.put("database", Map.of(
                "eventCount", eventCount,
                "roomCount", roomCount,
                "courseCount", courseCount
            ));
            
            // Add detailed sample event data to debug room relationships
            if (eventCount > 0) {
                List<Event> sampleEvents = eventRepository.findAllForAnalytics().stream().limit(3).collect(Collectors.toList());
                List<Map<String, Object>> samples = new ArrayList<>();
                
                for (Event event : sampleEvents) {
                    Map<String, Object> eventData = new HashMap<>();
                    eventData.put("id", event.getId());
                    eventData.put("title", event.getTitle());
                    eventData.put("date", event.getDate() != null ? event.getDate().toString() : "NULL");
                    
                    // Detailed room information
                    if (event.getRoom() != null) {
                        Map<String, Object> roomData = new HashMap<>();
                        roomData.put("id", event.getRoom().getId());
                        roomData.put("name", event.getRoom().getName());
                        eventData.put("room", roomData);
                        eventData.put("hasRoomRelationship", true);
                    } else {
                        eventData.put("room", "NULL");
                        eventData.put("hasRoomRelationship", false);
                    }
                    
                    samples.add(eventData);
                }
                testResponse.put("sampleEvents", samples);
            }
            
            log.info("🧪 Database counts - Events: {}, Rooms: {}, Courses: {}", eventCount, roomCount, courseCount);
            log.info("🧪 Test response created successfully");
            return ResponseEntity.ok(testResponse);
        } catch (Exception e) {
            log.error("❌ Test analytics failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Test failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/analytics")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSchedulingAnalytics(@RequestParam(required = false) String timeRange) {
        try {
            log.info("📊 Getting comprehensive scheduling analytics - timeRange: {}", timeRange);
            
            // Simple test response first
            if (timeRange == null) {
                timeRange = "week";
                log.info("📊 Using default timeRange: {}", timeRange);
            }
            
            log.info("📊 Calling analyticsService.getComprehensiveAnalytics...");
            Map<String, Object> analytics = analyticsService.getComprehensiveAnalytics(timeRange);
            log.info("📊 Analytics data retrieved successfully, size: {}", analytics.size());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("❌ Analytics failed: {}", e.getMessage(), e);
            e.printStackTrace(); // Print full stack trace for debugging
            return ResponseEntity.status(500).body("Error fetching analytics: " + e.getMessage());
        }
    }
    
    @GetMapping("/analytics/room-utilization")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getRoomUtilization() {
        try {
            log.info("📊 Getting room utilization statistics");
            Map<String, Object> roomStats = analyticsService.getRoomUtilizationStats();
            return ResponseEntity.ok(roomStats);
        } catch (Exception e) {
            log.error("❌ Room utilization analytics failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching room utilization: " + e.getMessage());
        }
    }
    
    @GetMapping("/analytics/time-distribution")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getTimeDistribution() {
        try {
            log.info("📊 Getting time distribution statistics");
            Map<String, Object> timeStats = analyticsService.getTimeDistributionStats();
            return ResponseEntity.ok(timeStats);
        } catch (Exception e) {
            log.error("❌ Time distribution analytics failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching time distribution: " + e.getMessage());
        }
    }
    
    @GetMapping("/analytics/subject-room-matching")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSubjectRoomMatching() {
        try {
            log.info("📊 Getting subject-room matching statistics");
            Map<String, Object> matchingStats = analyticsService.getSubjectRoomMatchingStats();
            return ResponseEntity.ok(matchingStats);
        } catch (Exception e) {
            log.error("❌ Subject-room matching analytics failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching subject-room matching stats: " + e.getMessage());
        }
    }
}
