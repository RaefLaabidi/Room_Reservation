package com.reservation.service;

import com.google.api.services.calendar.Calendar;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventAttendee;
import com.reservation.config.GoogleCalendarConfig;
import com.reservation.repository.EventRepository;
import com.reservation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoogleCalendarService {
    
    private final GoogleCalendarConfig calendarConfig;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private Calendar googleCalendar;

    @Autowired
    public GoogleCalendarService(GoogleCalendarConfig calendarConfig, 
                                EventRepository eventRepository,
                                UserRepository userRepository) {
        this.calendarConfig = calendarConfig;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        
        log.info("📅 GoogleCalendarService initialized - Enabled: {}", calendarConfig.isGoogleCalendarEnabled());
    }
    
    @Autowired(required = false)
    public void setGoogleCalendar(Calendar googleCalendar) {
        this.googleCalendar = googleCalendar;
    }

    /**
     * Sync admin schedule to Google Calendar with student email associations
     */
    public Map<String, Object> syncAdminScheduleToGoogleCalendar(String adminCalendarId) {
        if (!calendarConfig.isGoogleCalendarEnabled() || googleCalendar == null) {
            log.warn("📅 Google Calendar integration is disabled");
            return Map.of("success", false, "message", "Google Calendar integration is disabled");
        }

        try {
            // Get all events from admin system (current week + next 4 weeks)
            LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusWeeks(4);
            
            List<com.reservation.model.entity.Event> adminEvents = eventRepository.findByDateBetween(startDate, endDate);
            
            int syncedCount = 0;
            int errorCount = 0;
            
            for (com.reservation.model.entity.Event adminEvent : adminEvents) {
                try {
                    // Debug logging
                    log.info("🔍 Processing event: ID={}, Title='{}', Course={}, Type={}, Date={}, StartTime={}, EndTime={}", 
                        adminEvent.getId(),
                        adminEvent.getTitle(),
                        adminEvent.getCourse() != null ? adminEvent.getCourse().getName() : "null",
                        adminEvent.getType(),
                        adminEvent.getDate(),
                        adminEvent.getStartTime(),
                        adminEvent.getEndTime()
                    );
                    
                    // Only sync events that don't already have Google Calendar ID
                    if (adminEvent.getGoogleEventId() == null || adminEvent.getGoogleEventId().isEmpty()) {
                        String googleEventId = createGoogleCalendarEvent(adminEvent, adminCalendarId);
                        if (googleEventId != null) {
                            // Update the admin event with Google Calendar ID
                            adminEvent.setGoogleEventId(googleEventId);
                            eventRepository.save(adminEvent);
                            syncedCount++;
                            log.info("📅 ✅ Synced event: {} -> Google ID: {}", adminEvent.getTitle(), googleEventId);
                        } else {
                            errorCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ Failed to sync event {}: {}", adminEvent.getId(), e.getMessage());
                    errorCount++;
                }
            }
            
            return Map.of(
                "success", true,
                "totalEvents", adminEvents.size(),
                "syncedEvents", syncedCount,
                "errorCount", errorCount,
                "message", String.format("Synced %d events to Google Calendar", syncedCount)
            );
            
        } catch (Exception e) {
            log.error("❌ Error syncing admin schedule to Google Calendar: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Error syncing to Google Calendar: " + e.getMessage());
        }
    }

    /**
     * Create Google Calendar event with student attendees
     */
    private String createGoogleCalendarEvent(com.reservation.model.entity.Event adminEvent, String calendarId) {
        try {
            // Build a better event title
            String eventTitle = buildEventTitle(adminEvent);
            
            Event googleEvent = new Event()
                .setSummary(eventTitle)
                .setDescription(buildEventDescription(adminEvent));

            // Set start time
            EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(
                    adminEvent.getStartTime().atDate(adminEvent.getDate()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ))
                .setTimeZone(ZoneId.systemDefault().getId());
            googleEvent.setStart(start);

            // Set end time
            EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(
                    adminEvent.getEndTime().atDate(adminEvent.getDate()).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ))
                .setTimeZone(ZoneId.systemDefault().getId());
            googleEvent.setEnd(end);

            // Set location
            if (adminEvent.getRoom() != null) {
                googleEvent.setLocation(adminEvent.getRoom().getName());
            }

            // Add attendees (students associated with the course)
            List<EventAttendee> attendees = getEventAttendees(adminEvent);
            if (!attendees.isEmpty()) {
                googleEvent.setAttendees(attendees);
            }

            // Create the event in Google Calendar
            Event createdEvent = googleCalendar.events().insert(calendarId, googleEvent).execute();
            log.info("📅 ✅ Created Google Calendar event: {}", createdEvent.getId());
            
            return createdEvent.getId();
            
        } catch (IOException e) {
            log.error("❌ Error creating Google Calendar event: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Build a proper event title based on available information
     */
    private String buildEventTitle(com.reservation.model.entity.Event adminEvent) {
        // Priority 1: Use explicit title if available and not generic
        if (adminEvent.getTitle() != null && 
            !adminEvent.getTitle().isEmpty() && 
            !adminEvent.getTitle().equals("DS - Office Hours")) {
            return adminEvent.getTitle();
        }
        
        // Priority 2: Use course information
        if (adminEvent.getCourse() != null) {
            String courseTitle = adminEvent.getCourse().getName();
            if (courseTitle != null && !courseTitle.isEmpty()) {
                // Add subject if available
                if (adminEvent.getCourse().getSubject() != null && !adminEvent.getCourse().getSubject().isEmpty()) {
                    return adminEvent.getCourse().getSubject() + " - " + courseTitle;
                }
                return courseTitle;
            }
        }
        
        // Priority 3: Use event type
        if (adminEvent.getType() != null) {
            String typeName = adminEvent.getType().toString();
            // Make it more readable
            switch (adminEvent.getType()) {
                case COURSE:
                    return "Course Session";
                case DEFENSE:
                    return "Thesis Defense";
                case MEETING:
                    return "Academic Meeting";
                default:
                    return typeName.substring(0, 1).toUpperCase() + typeName.substring(1).toLowerCase();
            }
        }
        
        // Priority 4: Add teacher name if available
        if (adminEvent.getTeacher() != null && adminEvent.getTeacher().getName() != null) {
            return "Class with " + adminEvent.getTeacher().getName();
        }
        
        // Fallback
        return "University Event";
    }

    /**
     * Build detailed event description
     */
    private String buildEventDescription(com.reservation.model.entity.Event adminEvent) {
        StringBuilder description = new StringBuilder();
        description.append("📚 University Class Event\n\n");
        
        if (adminEvent.getCourse() != null) {
            description.append("Course: ").append(adminEvent.getCourse().getName()).append("\n");
            description.append("Subject: ").append(adminEvent.getCourse().getSubject()).append("\n");
        }
        
        if (adminEvent.getTeacher() != null) {
            description.append("Instructor: ").append(adminEvent.getTeacher().getEmail()).append("\n");
        }
        
        if (adminEvent.getRoom() != null) {
            description.append("Room: ").append(adminEvent.getRoom().getName()).append("\n");
        }
        
        if (adminEvent.getDescription() != null) {
            description.append("\nDetails: ").append(adminEvent.getDescription());
        }
        
        description.append("\n\n🔗 Synced from University Reservation System");
        
        return description.toString();
    }

    /**
     * Get list of student attendees for the event
     */
    private List<EventAttendee> getEventAttendees(com.reservation.model.entity.Event adminEvent) {
        List<EventAttendee> attendees = new ArrayList<>();
        
        // Add the teacher/professor as an attendee
        if (adminEvent.getTeacher() != null && adminEvent.getTeacher().getEmail() != null) {
            EventAttendee teacherAttendee = new EventAttendee()
                .setEmail(adminEvent.getTeacher().getEmail())
                .setDisplayName(adminEvent.getTeacher().getName())
                .setOrganizer(true);
            attendees.add(teacherAttendee);
        }
        
        // TODO: Add students based on course enrollment
        // For now, we can add all users with STUDENT role as a demo
        try {
            List<com.reservation.model.entity.User> students = userRepository.findByRole(
                com.reservation.model.enums.Role.STUDENT
            );
            
            for (com.reservation.model.entity.User student : students) {
                if (student.getEmail() != null && !student.getEmail().isEmpty()) {
                    EventAttendee studentAttendee = new EventAttendee()
                        .setEmail(student.getEmail())
                        .setDisplayName(student.getName())
                        .setOptional(false);
                    attendees.add(studentAttendee);
                    
                    // Limit to reasonable number of attendees
                    if (attendees.size() >= 50) break;
                }
            }
            
        } catch (Exception e) {
            log.warn("⚠️  Could not fetch students for event attendees: {}", e.getMessage());
        }
        
        return attendees;
    }

    /**
     * Clear existing Google Calendar sync data and force re-sync
     */
    public Map<String, Object> clearAndResyncCalendar(String adminCalendarId) {
        log.info("🔄 Clearing existing Google Calendar sync data and re-syncing...");
        
        try {
            // Clear Google Event IDs from all events to force re-sync
            LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate endDate = startDate.plusWeeks(4);
            List<com.reservation.model.entity.Event> events = eventRepository.findByDateBetween(startDate, endDate);
            
            int clearedCount = 0;
            for (com.reservation.model.entity.Event event : events) {
                if (event.getGoogleEventId() != null && !event.getGoogleEventId().isEmpty()) {
                    event.setGoogleEventId(null);
                    eventRepository.save(event);
                    clearedCount++;
                }
            }
            
            log.info("🧹 Cleared {} existing Google Calendar sync records", clearedCount);
            
            // Now perform fresh sync
            return syncAdminScheduleToGoogleCalendar(adminCalendarId);
            
        } catch (Exception e) {
            log.error("❌ Error clearing and re-syncing calendar: {}", e.getMessage(), e);
            return Map.of("success", false, "message", "Error clearing and re-syncing: " + e.getMessage());
        }
    }

    /**
     * Get events for debugging purposes
     */
    public List<com.reservation.model.entity.Event> getEventsForDebugging(LocalDate startDate, LocalDate endDate) {
        return eventRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Get weekly schedule for professor
     */
    public Map<String, Object> getProfessorWeeklySchedule(String email) {
        log.info("📅 Getting weekly schedule for professor: {}", email);
        
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<com.reservation.model.entity.Event> events = eventRepository.findByProfessorEmailAndDateBetween(email, startOfWeek, endOfWeek);
        
        Map<String, Object> response = new HashMap<>();
        response.put("professor", email);
        response.put("weekStart", startOfWeek);
        response.put("weekEnd", endOfWeek);
        response.put("events", events.stream().map(this::eventToMap).collect(Collectors.toList()));
        response.put("googleCalendarEnabled", calendarConfig.isGoogleCalendarEnabled());
        
        log.info("📅 ✅ Found {} events for professor {} this week", events.size(), email);
        return response;
    }

    /**
     * Get weekly schedule for student
     */
    public Map<String, Object> getStudentWeeklySchedule(String email) {
        log.info("📅 Getting weekly schedule for student: {}", email);
        
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<com.reservation.model.entity.Event> events = eventRepository.findByStudentEmailAndDateBetween(startOfWeek, endOfWeek);
        
        Map<String, Object> response = new HashMap<>();
        response.put("student", email);
        response.put("weekStart", startOfWeek);
        response.put("weekEnd", endOfWeek);
        response.put("events", events.stream().map(this::eventToMap).collect(Collectors.toList()));
        response.put("googleCalendarEnabled", calendarConfig.isGoogleCalendarEnabled());
        
        log.info("📅 ✅ Found {} events for student {} this week", events.size(), email);
        return response;
    }

    /**
     * Create event in Google Calendar when created in our system
     */
    public String createEventInGoogleCalendar(com.reservation.model.entity.Event reservationEvent, String calendarId) {
        return createGoogleCalendarEvent(reservationEvent, calendarId);
    }

    /**
     * Sync events from Google Calendar (placeholder for future implementation)
     */
    public Map<String, Object> syncFromGoogleCalendar(String calendarId) {
        // For now, redirect to the admin sync functionality
        return syncAdminScheduleToGoogleCalendar(calendarId);
    }

    private Map<String, Object> eventToMap(com.reservation.model.entity.Event event) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", event.getId());
        eventMap.put("title", event.getTitle());
        eventMap.put("date", event.getDate());
        eventMap.put("startTime", event.getStartTime());
        eventMap.put("endTime", event.getEndTime());
        eventMap.put("description", event.getDescription());
        eventMap.put("googleEventId", event.getGoogleEventId());
        
        if (event.getRoom() != null) {
            eventMap.put("room", event.getRoom().getName());
            eventMap.put("location", event.getRoom().getName());
        }
        if (event.getCourse() != null) {
            eventMap.put("course", event.getCourse().getName());
        }
        if (event.getTeacher() != null) {
            eventMap.put("professor", event.getTeacher().getEmail());
            eventMap.put("teacher", event.getTeacher().getEmail());
        }
        
        return eventMap;
    }
}
