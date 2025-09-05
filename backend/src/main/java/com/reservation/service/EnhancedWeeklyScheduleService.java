package com.reservation.service;

import com.reservation.model.entity.*;
import com.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedWeeklyScheduleService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Transactional
    public WeeklyScheduleResult createWeeklySchedule(WeeklyScheduleRequest request) {
        log.info("Creating weekly schedule for week starting: {}", request.getWeekStartDate());
        
        WeeklyScheduleResult result = new WeeklyScheduleResult();
        result.setWeekStartDate(request.getWeekStartDate());
        result.setWeekEndDate(request.getWeekStartDate().plusDays(6));
        
        List<ScheduledCourseEvent> scheduledEvents = new ArrayList<>();
        List<SchedulingError> errors = new ArrayList<>();
        
        // Sort courses by priority (higher priority first)
        List<CourseScheduleRequest> sortedCourses = request.getCourses().stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
        
        for (CourseScheduleRequest courseRequest : sortedCourses) {
            try {
                List<ScheduledCourseEvent> courseEvents = scheduleCourse(
                    courseRequest, 
                    request.getWeekStartDate(), 
                    scheduledEvents
                );
                scheduledEvents.addAll(courseEvents);
                result.setSuccessfulCourses(result.getSuccessfulCourses() + 1);
            } catch (Exception e) {
                log.error("Failed to schedule course {}: {}", courseRequest.getCourseId(), e.getMessage());
                errors.add(new SchedulingError(courseRequest.getCourseId(), e.getMessage()));
                result.setFailedCourses(result.getFailedCourses() + 1);
            }
        }
        
        // Save all scheduled events to database
        for (ScheduledCourseEvent scheduledEvent : scheduledEvents) {
            saveScheduledEvent(scheduledEvent);
        }
        
        result.setScheduledEvents(scheduledEvents);
        result.setErrors(errors);
        result.setTotalCourses(request.getCourses().size());
        result.setSuccess(errors.isEmpty());
        
        log.info("Weekly schedule created. Successful: {}, Failed: {}", 
                result.getSuccessfulCourses(), result.getFailedCourses());
        
        return result;
    }
    
    private List<ScheduledCourseEvent> scheduleCourse(CourseScheduleRequest courseRequest, 
                                                     LocalDate weekStart, 
                                                     List<ScheduledCourseEvent> existingEvents) {
        
        Course course = courseRepository.findById(courseRequest.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseRequest.getCourseId()));
        
        List<ScheduledCourseEvent> courseEvents = new ArrayList<>();
        int sessionsToSchedule = course.getSessionsPerWeek();
        
        for (int session = 0; session < sessionsToSchedule; session++) {
            ScheduledCourseEvent event = scheduleIndividualSession(
                course, 
                courseRequest, 
                weekStart, 
                existingEvents,
                courseEvents,
                session
            );
            
            if (event != null) {
                courseEvents.add(event);
            } else {
                throw new RuntimeException("Could not find suitable time slot for session " + (session + 1));
            }
        }
        
        return courseEvents;
    }
    
    private ScheduledCourseEvent scheduleIndividualSession(Course course,
                                                          CourseScheduleRequest courseRequest,
                                                          LocalDate weekStart,
                                                          List<ScheduledCourseEvent> existingEvents,
                                                          List<ScheduledCourseEvent> currentCourseEvents,
                                                          int sessionNumber) {
        
        // Find best teacher for this course
        User teacher = findBestTeacher(course.getSubject(), existingEvents, currentCourseEvents);
        if (teacher == null) {
            throw new RuntimeException("No available teacher found for subject: " + course.getSubject());
        }
        
        // Find available time slots for this week
        List<UniversityTimeSlot> availableSlots = findAvailableTimeSlots(
            teacher, 
            weekStart, 
            course.getDurationHours(),
            existingEvents,
            currentCourseEvents
        );
        
        if (availableSlots.isEmpty()) {
            throw new RuntimeException("No available time slots found");
        }
        
        // Find suitable room for the best time slot
        UniversityTimeSlot bestSlot = availableSlots.get(0);
        Room room = findAvailableRoom(bestSlot, courseRequest.getStudentCount(), course.getPreferredRoomType());
        
        if (room == null) {
            throw new RuntimeException("No available room found");
        }
        
        return ScheduledCourseEvent.builder()
                .courseId(course.getId())
                .courseName(course.getName())
                .teacher(TeacherInfo.builder()
                        .id(teacher.getId())
                        .name(teacher.getName())
                        .email(teacher.getEmail())
                        .build())
                .room(RoomInfo.builder()
                        .id(room.getId())
                        .name(room.getName())
                        .location(room.getLocation())
                        .capacity(room.getCapacity())
                        .build())
                .startDateTime(bestSlot.getStartDateTime())
                .endDateTime(bestSlot.getEndDateTime())
                .studentCount(courseRequest.getStudentCount())
                .priority(courseRequest.getPriority())
                .sessionNumber(sessionNumber + 1)
                .build();
    }
    
    private User findBestTeacher(String subject, 
                                List<ScheduledCourseEvent> existingEvents, 
                                List<ScheduledCourseEvent> currentCourseEvents) {
        
        List<TeacherSubject> qualifiedTeachers = teacherSubjectRepository.findBySubjectOrderByExpertiseDesc(subject);
        
        for (TeacherSubject ts : qualifiedTeachers) {
            User teacher = ts.getTeacher();
            if (teacher.getRole() == com.reservation.model.enums.Role.TEACHER) {
                // Check if teacher is not overloaded
                long teacherEvents = existingEvents.stream()
                        .filter(e -> e.getTeacher().getId().equals(teacher.getId()))
                        .count();
                long currentEvents = currentCourseEvents.stream()
                        .filter(e -> e.getTeacher().getId().equals(teacher.getId()))
                        .count();
                
                // Limit to 4 sessions per teacher per week
                if (teacherEvents + currentEvents < 4) {
                    return teacher;
                }
            }
        }
        
        return null;
    }
    
    private List<UniversityTimeSlot> findAvailableTimeSlots(User teacher,
                                                           LocalDate weekStart,
                                                           int durationHours,
                                                           List<ScheduledCourseEvent> existingEvents,
                                                           List<ScheduledCourseEvent> currentCourseEvents) {
        
        List<UniversityTimeSlot> availableSlots = new ArrayList<>();
        
        for (int day = 0; day < 7; day++) {
            LocalDate currentDate = weekStart.plusDays(day);
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            
            // Skip Sunday (no work)
            if (dayOfWeek == 7) continue;
            
            List<UniversityTimeSlot> daySlots = generateUniversityTimeSlots(currentDate, dayOfWeek, durationHours);
            
            for (UniversityTimeSlot slot : daySlots) {
                if (isTeacherAvailable(teacher, slot) && 
                    !hasConflictWithExistingEvents(teacher, slot, existingEvents, currentCourseEvents)) {
                    availableSlots.add(slot);
                }
            }
        }
        
        // Sort by preference score
        availableSlots.sort((a, b) -> Integer.compare(b.getPreferenceScore(), a.getPreferenceScore()));
        
        return availableSlots;
    }
    
    private List<UniversityTimeSlot> generateUniversityTimeSlots(LocalDate date, int dayOfWeek, int durationHours) {
        List<UniversityTimeSlot> slots = new ArrayList<>();
        
        // Morning slots: 9:00 AM - 12:15 PM (all days except Sunday)
        if (dayOfWeek != 7) {
            slots.addAll(generateSlotsInTimeRange(date, 
                LocalTime.of(9, 0), 
                LocalTime.of(12, 15), 
                durationHours, 
                "MORNING",
                8)); // High preference for morning
        }
        
        // Afternoon slots: 1:30 PM - 4:45 PM (Monday, Tuesday, Thursday, Friday only)
        if (dayOfWeek != 3 && dayOfWeek != 6 && dayOfWeek != 7) {
            slots.addAll(generateSlotsInTimeRange(date, 
                LocalTime.of(13, 30), 
                LocalTime.of(16, 45), 
                durationHours, 
                "AFTERNOON",
                6)); // Medium preference for afternoon
        }
        
        return slots;
    }
    
    private List<UniversityTimeSlot> generateSlotsInTimeRange(LocalDate date, 
                                                            LocalTime startTime, 
                                                            LocalTime endTime, 
                                                            int durationHours,
                                                            String period,
                                                            int baseScore) {
        List<UniversityTimeSlot> slots = new ArrayList<>();
        LocalTime current = startTime;
        
        while (current.plusHours(durationHours).isBefore(endTime) || 
               current.plusHours(durationHours).equals(endTime)) {
            
            LocalDateTime slotStart = LocalDateTime.of(date, current);
            LocalDateTime slotEnd = slotStart.plusHours(durationHours);
            
            int preferenceScore = baseScore;
            
            // Prefer earlier times in each period
            if (current.equals(startTime)) {
                preferenceScore += 2; // First slot bonus
            }
            
            slots.add(UniversityTimeSlot.builder()
                    .startDateTime(slotStart)
                    .endDateTime(slotEnd)
                    .period(period)
                    .preferenceScore(preferenceScore)
                    .build());
            
            current = current.plusMinutes(30); // Try every 30 minutes
        }
        
        return slots;
    }
    
    private boolean isTeacherAvailable(User teacher, UniversityTimeSlot slot) {
        int dayOfWeek = slot.getStartDateTime().getDayOfWeek().getValue();
        LocalTime slotTime = slot.getStartDateTime().toLocalTime();
        
        List<TeacherAvailability> availability = teacherAvailabilityRepository
                .findByTeacherIdAndDayOfWeek(teacher.getId(), dayOfWeek);
        
        for (TeacherAvailability ta : availability) {
            if (ta.getIsAvailable() && 
                slotTime.isAfter(ta.getStartTime().minusMinutes(1)) && 
                slot.getEndDateTime().toLocalTime().isBefore(ta.getEndTime().plusMinutes(1))) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasConflictWithExistingEvents(User teacher, 
                                                 UniversityTimeSlot slot,
                                                 List<ScheduledCourseEvent> existingEvents,
                                                 List<ScheduledCourseEvent> currentCourseEvents) {
        
        // Check against already scheduled events in this request
        for (ScheduledCourseEvent event : existingEvents) {
            if (event.getTeacher().getId().equals(teacher.getId()) &&
                timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(),
                               event.getStartDateTime(), event.getEndDateTime())) {
                return true;
            }
        }
        
        // Check against current course events being scheduled
        for (ScheduledCourseEvent event : currentCourseEvents) {
            if (event.getTeacher().getId().equals(teacher.getId()) &&
                timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(),
                               event.getStartDateTime(), event.getEndDateTime())) {
                return true;
            }
        }
        
        // Check against existing database events
        List<Event> dbEvents = eventRepository.findEventsByTeacherAndDateRange(
                teacher.getId(),
                slot.getStartDateTime().toLocalDate(),
                slot.getEndDateTime().toLocalDate()
        );
        
        for (Event event : dbEvents) {
            LocalDateTime eventStart = LocalDateTime.of(event.getDate(), event.getStartTime());
            LocalDateTime eventEnd = LocalDateTime.of(event.getDate(), event.getEndTime());
            if (timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(), eventStart, eventEnd)) {
                return true;
            }
        }
        
        return false;
    }
    
    private Room findAvailableRoom(UniversityTimeSlot slot, Integer studentCount, String preferredRoomType) {
        List<Room> allRooms = roomRepository.findAll();
        
        // Filter by capacity first
        List<Room> suitableRooms = allRooms.stream()
                .filter(room -> studentCount == null || room.getCapacity() >= studentCount)
                .collect(Collectors.toList());
        
        // Sort by preference
        suitableRooms.sort((r1, r2) -> {
            int score1 = calculateRoomScore(r1, preferredRoomType);
            int score2 = calculateRoomScore(r2, preferredRoomType);
            return Integer.compare(score2, score1);
        });
        
        for (Room room : suitableRooms) {
            if (isRoomAvailable(room, slot)) {
                return room;
            }
        }
        
        return null;
    }
    
    private int calculateRoomScore(Room room, String preferredRoomType) {
        int score = 0;
        
        if (preferredRoomType != null && 
            room.getLocation().toLowerCase().contains(preferredRoomType.toLowerCase())) {
            score += 10;
        }
        
        // Prefer rooms with moderate capacity (not too big, not too small)
        if (room.getCapacity() >= 20 && room.getCapacity() <= 50) {
            score += 5;
        }
        
        return score;
    }
    
    private boolean isRoomAvailable(Room room, UniversityTimeSlot slot) {
        List<Event> roomEvents = eventRepository.findEventsByRoomAndDateRange(
                room.getId(),
                slot.getStartDateTime().toLocalDate(),
                slot.getEndDateTime().toLocalDate()
        );
        
        for (Event event : roomEvents) {
            LocalDateTime eventStart = LocalDateTime.of(event.getDate(), event.getStartTime());
            LocalDateTime eventEnd = LocalDateTime.of(event.getDate(), event.getEndTime());
            if (timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(), eventStart, eventEnd)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean timeSlotsOverlap(LocalDateTime start1, LocalDateTime end1,
                                   LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    private void saveScheduledEvent(ScheduledCourseEvent scheduledEvent) {
        try {
            // Fetch actual entities from database using IDs
            User teacher = userRepository.findById(scheduledEvent.getTeacher().getId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found: " + scheduledEvent.getTeacher().getId()));
            Room room = roomRepository.findById(scheduledEvent.getRoom().getId())
                    .orElseThrow(() -> new RuntimeException("Room not found: " + scheduledEvent.getRoom().getId()));
            
            log.info("🔧 Saving event with Room: ID={}, Name='{}'", room.getId(), room.getName());
            
            // NEW: Fetch the course entity to create proper relationship
            Course course = courseRepository.findById(scheduledEvent.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found: " + scheduledEvent.getCourseId()));
            
            Event event = Event.builder()
                    .title(scheduledEvent.getCourseName() + " (Session " + scheduledEvent.getSessionNumber() + ")")
                    .description("Auto-scheduled course - Priority: " + scheduledEvent.getPriority())
                    .date(scheduledEvent.getStartDateTime().toLocalDate())
                    .startTime(scheduledEvent.getStartDateTime().toLocalTime())
                    .endTime(scheduledEvent.getEndDateTime().toLocalTime())
                    .teacher(teacher)
                    .room(room)
                    .course(course) // NEW: Link to course entity
                    .type(com.reservation.model.enums.EventType.COURSE)
                    .status(com.reservation.model.enums.EventStatus.SCHEDULED)
                    .expectedParticipants(scheduledEvent.getStudentCount()) // NEW: Add expected participants
                    .build();
            
            Event savedEvent = eventRepository.save(event);
            log.info("✅ Event saved with ID={}, Room relationship: {}", 
                savedEvent.getId(), 
                savedEvent.getRoom() != null ? savedEvent.getRoom().getName() : "NULL");
                
        } catch (Exception e) {
            log.error("❌ Error saving scheduled event: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // Data classes
    public static class WeeklyScheduleRequest {
        private LocalDate weekStartDate;
        private List<CourseScheduleRequest> courses;
        
        // Getters and setters
        public LocalDate getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
        public List<CourseScheduleRequest> getCourses() { return courses; }
        public void setCourses(List<CourseScheduleRequest> courses) { this.courses = courses; }
    }
    
    public static class CourseScheduleRequest {
        private Long courseId;
        private Integer priority;
        private Integer studentCount;
        
        // Getters and setters
        public Long getCourseId() { return courseId; }
        public void setCourseId(Long courseId) { this.courseId = courseId; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public Integer getStudentCount() { return studentCount; }
        public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ScheduledCourseEvent {
        private Long courseId;
        private String courseName;
        private TeacherInfo teacher;
        private RoomInfo room;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Integer studentCount;
        private Integer priority;
        private Integer sessionNumber;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeacherInfo {
        private Long id;
        private String name;
        private String email;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoomInfo {
        private Long id;
        private String name;
        private String location;
        private Integer capacity;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UniversityTimeSlot {
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String period; // "MORNING" or "AFTERNOON"
        private Integer preferenceScore;
    }
    
    public static class WeeklyScheduleResult {
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private int totalCourses;
        private int successfulCourses;
        private int failedCourses;
        private boolean success;
        private List<ScheduledCourseEvent> scheduledEvents = new ArrayList<>();
        private List<SchedulingError> errors = new ArrayList<>();
        
        // Getters and setters
        public LocalDate getWeekStartDate() { return weekStartDate; }
        public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
        public LocalDate getWeekEndDate() { return weekEndDate; }
        public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }
        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
        public int getSuccessfulCourses() { return successfulCourses; }
        public void setSuccessfulCourses(int successfulCourses) { this.successfulCourses = successfulCourses; }
        public int getFailedCourses() { return failedCourses; }
        public void setFailedCourses(int failedCourses) { this.failedCourses = failedCourses; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<ScheduledCourseEvent> getScheduledEvents() { return scheduledEvents; }
        public void setScheduledEvents(List<ScheduledCourseEvent> scheduledEvents) { this.scheduledEvents = scheduledEvents; }
        public List<SchedulingError> getErrors() { return errors; }
        public void setErrors(List<SchedulingError> errors) { this.errors = errors; }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SchedulingError {
        private Long courseId;
        private String message;
    }
}
