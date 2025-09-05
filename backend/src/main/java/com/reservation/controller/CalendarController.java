package com.reservation.controller;

import com.reservation.service.GoogleCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CalendarController {
    
    private final GoogleCalendarService googleCalendarService;
    
    /**
     * Get professor's weekly teaching schedule
     */
    @GetMapping("/professor/{professorEmail}/weekly")
    public ResponseEntity<Map<String, Object>> getProfessorWeeklySchedule(
            @PathVariable String professorEmail) {
        
        try {
            Map<String, Object> schedule = googleCalendarService.getProfessorWeeklySchedule(professorEmail);
            
            return ResponseEntity.ok(schedule);
            
        } catch (Exception e) {
            log.error("Error getting professor weekly schedule: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get weekly schedule: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get student's weekly class schedule
     */
    @GetMapping("/student/{studentEmail}/weekly")
    public ResponseEntity<Map<String, Object>> getStudentWeeklySchedule(
            @PathVariable String studentEmail) {
        
        try {
            Map<String, Object> schedule = googleCalendarService.getStudentWeeklySchedule(studentEmail);
            
            return ResponseEntity.ok(schedule);
            
        } catch (Exception e) {
            log.error("Error getting student weekly schedule: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get weekly schedule: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get general weekly schedule (all events)
     */
    @GetMapping("/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklySchedule() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "General weekly schedule endpoint - specify professor or student email");
            response.put("availableEndpoints", List.of(
                "/api/calendar/professor/{email}/weekly",
                "/api/calendar/student/{email}/weekly"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting weekly schedule: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get weekly schedule: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Sync admin schedule to Google Calendar
     */
    @PostMapping("/sync-admin-schedule")
    public ResponseEntity<Map<String, Object>> syncAdminScheduleToGoogle(
            @RequestParam(defaultValue = "primary") String calendarId) {
        
        try {
            Map<String, Object> syncResult = googleCalendarService.syncAdminScheduleToGoogleCalendar(calendarId);
            
            return ResponseEntity.ok(syncResult);
            
        } catch (Exception e) {
            log.error("Error syncing admin schedule to Google Calendar: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to sync admin schedule: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Sync with Google Calendar
     */
    @PostMapping("/sync/{calendarId}")
    public ResponseEntity<Map<String, Object>> syncWithGoogleCalendar(
            @PathVariable String calendarId) {
        
        try {
            Map<String, Object> syncResult = googleCalendarService.syncFromGoogleCalendar(calendarId);
            
            return ResponseEntity.ok(syncResult);
            
        } catch (Exception e) {
            log.error("Error syncing with Google Calendar: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to sync calendar: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    /**
     * Get calendar integration status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCalendarStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("googleCalendarEnabled", googleCalendarService != null);
        status.put("features", List.of(
            "Weekly schedule for professors",
            "Weekly schedule for students", 
            "Google Calendar sync",
            "Real-time calendar integration",
            "Automatic event creation"
        ));
        status.put("message", "Calendar integration status");
        
        return ResponseEntity.ok(status);
    }
}
