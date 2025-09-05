package com.reservation.controller;

import com.reservation.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "http://localhost:3000")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    /**
     * Sync admin schedule to Google Calendar with student emails as attendees
     */
    @PostMapping("/sync/admin")
    public ResponseEntity<Map<String, Object>> syncAdminSchedule() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Use "primary" as the default admin calendar ID
            Map<String, Object> result = googleCalendarService.syncAdminScheduleToGoogleCalendar("primary");
            
            response.put("success", true);
            response.put("message", "Admin schedule successfully synchronized with Google Calendar");
            response.put("syncData", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to sync admin schedule: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Sync primary calendar (for WeeklySchedule component)
     */
    @PostMapping("/sync/primary")
    public ResponseEntity<Map<String, Object>> syncPrimaryCalendar() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = googleCalendarService.syncAdminScheduleToGoogleCalendar("primary");
            
            response.put("success", true);
            response.put("message", "Calendar successfully synchronized");
            response.put("syncData", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to sync calendar: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Clear existing sync data and force re-sync with proper titles
     */
    @PostMapping("/sync/admin/clear-and-resync")
    public ResponseEntity<Map<String, Object>> clearAndResyncAdminSchedule() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Use "primary" as the default admin calendar ID
            Map<String, Object> result = googleCalendarService.clearAndResyncCalendar("primary");
            
            response.put("success", true);
            response.put("message", "Calendar cleared and re-synchronized with proper event titles");
            response.put("syncData", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to clear and re-sync calendar: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Debug endpoint to see what events are in the database
     */
    @GetMapping("/debug/events")
    public ResponseEntity<Map<String, Object>> debugEvents() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate startDate = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusWeeks(4);
            
            // Get events through the service to see what we're working with
            java.util.List<com.reservation.model.entity.Event> events = 
                googleCalendarService.getEventsForDebugging(startDate, endDate);
            
            java.util.List<Map<String, Object>> eventDetails = new java.util.ArrayList<>();
            
            for (com.reservation.model.entity.Event event : events) {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", event.getId());
                eventMap.put("title", event.getTitle());
                eventMap.put("type", event.getType());
                eventMap.put("date", event.getDate());
                eventMap.put("startTime", event.getStartTime());
                eventMap.put("endTime", event.getEndTime());
                eventMap.put("googleEventId", event.getGoogleEventId());
                
                // Course info
                if (event.getCourse() != null) {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("id", event.getCourse().getId());
                    courseInfo.put("name", event.getCourse().getName());
                    courseInfo.put("subject", event.getCourse().getSubject());
                    eventMap.put("course", courseInfo);
                } else {
                    eventMap.put("course", null);
                }
                
                // Teacher info
                if (event.getTeacher() != null) {
                    Map<String, Object> teacherInfo = new HashMap<>();
                    teacherInfo.put("id", event.getTeacher().getId());
                    teacherInfo.put("name", event.getTeacher().getName());
                    teacherInfo.put("email", event.getTeacher().getEmail());
                    eventMap.put("teacher", teacherInfo);
                } else {
                    eventMap.put("teacher", null);
                }
                
                // Room info
                if (event.getRoom() != null) {
                    Map<String, Object> roomInfo = new HashMap<>();
                    roomInfo.put("id", event.getRoom().getId());
                    roomInfo.put("name", event.getRoom().getName());
                    eventMap.put("room", roomInfo);
                } else {
                    eventMap.put("room", null);
                }
                
                eventDetails.add(eventMap);
            }
            
            response.put("success", true);
            response.put("totalEvents", events.size());
            response.put("events", eventDetails);
            response.put("dateRange", Map.of("start", startDate, "end", endDate));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error fetching debug info: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get calendar sync status
     */
    @GetMapping("/sync/status")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // You can add logic here to check if Google Calendar is properly configured
            response.put("isConfigured", true);
            response.put("lastSyncTime", System.currentTimeMillis());
            response.put("status", "ready");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("isConfigured", false);
            response.put("error", e.getMessage());
            response.put("status", "error");
            
            return ResponseEntity.status(500).body(response);
        }
    }
}
