package com.reservation.service;

import com.reservation.model.entity.*;
import com.reservation.model.enums.Role;
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
public class AutoSchedulingService {

    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    
    @Transactional
    public SchedulingResult generateSchedule(Long templateId) {
        log.info("Starting auto-scheduling for template ID: {}", templateId);
        
        ScheduleTemplate template = scheduleTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        SchedulingResult result = new SchedulingResult();
        result.setTemplateId(templateId);
        result.setTotalCourses(template.getCourseAssignments().size());
        
        List<ScheduledEvent> scheduledEvents = new ArrayList<>();
        List<SchedulingConflict> conflicts = new ArrayList<>();
        
        // Sort assignments by priority (higher priority first)
        List<TemplateCourseAssignment> sortedAssignments = template.getCourseAssignments()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .collect(Collectors.toList());
        
        for (TemplateCourseAssignment assignment : sortedAssignments) {
            try {
                ScheduledEvent event = scheduleAssignment(assignment, template, scheduledEvents);
                if (event != null) {
                    scheduledEvents.add(event);
                    result.setScheduledCourses(result.getScheduledCourses() + 1);
                } else {
                    conflicts.add(new SchedulingConflict(assignment, "No suitable time slot found"));
                    result.setFailedCourses(result.getFailedCourses() + 1);
                }
            } catch (Exception e) {
                log.error("Error scheduling assignment {}: {}", assignment.getId(), e.getMessage());
                conflicts.add(new SchedulingConflict(assignment, e.getMessage()));
                result.setFailedCourses(result.getFailedCourses() + 1);
            }
        }
        
        // Save successful schedules as events
        for (ScheduledEvent scheduledEvent : scheduledEvents) {
            saveScheduledEvent(scheduledEvent);
        }
        
        result.setScheduledEvents(scheduledEvents);
        result.setConflicts(conflicts);
        result.setSuccess(result.getFailedCourses() == 0);
        
        log.info("Auto-scheduling completed. Scheduled: {}, Failed: {}", 
                result.getScheduledCourses(), result.getFailedCourses());
        
        return result;
    }
    
    private ScheduledEvent scheduleAssignment(TemplateCourseAssignment assignment, 
                                            ScheduleTemplate template, 
                                            List<ScheduledEvent> existingEvents) {
        
        Course course = assignment.getCourse();
        User assignedTeacher = assignment.getAssignedTeacher();
        
        // Find available teacher if not assigned
        if (assignedTeacher == null) {
            assignedTeacher = findBestTeacher(course.getSubject());
            if (assignedTeacher == null) {
                throw new RuntimeException("No qualified teacher found for subject: " + course.getSubject());
            }
        }
        
        // Find suitable time slots
        List<TimeSlot> availableSlots = findAvailableTimeSlots(
                assignedTeacher, 
                template.getWeekStartDate(), 
                template.getWeekEndDate(),
                course.getDurationHours(),
                assignment,
                existingEvents
        );
        
        if (availableSlots.isEmpty()) {
            return null;
        }
        
        // Find suitable room
        TimeSlot bestSlot = availableSlots.get(0); // Take the first available slot
        Room suitableRoom = findSuitableRoom(bestSlot, assignment.getStudentCount(), course.getPreferredRoomType());
        
        if (suitableRoom == null) {
            throw new RuntimeException("No suitable room found");
        }
        
        return ScheduledEvent.builder()
                .assignment(assignment)
                .teacher(assignedTeacher)
                .room(suitableRoom)
                .startDateTime(bestSlot.getStartDateTime())
                .endDateTime(bestSlot.getEndDateTime())
                .build();
    }
    
    private User findBestTeacher(String subject) {
        List<TeacherSubject> teacherSubjects = teacherSubjectRepository.findBySubjectOrderByExpertiseDesc(subject);
        
        for (TeacherSubject ts : teacherSubjects) {
            if (ts.getTeacher().getRole() == Role.TEACHER) {
                return ts.getTeacher();
            }
        }
        
        return null;
    }
    
    private List<TimeSlot> findAvailableTimeSlots(User teacher, 
                                                 LocalDate weekStart, 
                                                 LocalDate weekEnd,
                                                 int durationHours,
                                                 TemplateCourseAssignment assignment,
                                                 List<ScheduledEvent> existingEvents) {
        
        List<TimeSlot> availableSlots = new ArrayList<>();
        
        // Get teacher's weekly availability
        List<TeacherAvailability> weeklyAvailability = teacherAvailabilityRepository.findByTeacherId(teacher.getId());
        
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
            
            // Check if teacher is available on this day
            List<TeacherAvailability> dayAvailability = weeklyAvailability.stream()
                    .filter(ta -> ta.getDayOfWeek().equals(dayOfWeek) && ta.getIsAvailable())
                    .collect(Collectors.toList());
            
            for (TeacherAvailability availability : dayAvailability) {
                List<TimeSlot> daySlots = generateTimeSlots(
                        date, 
                        availability.getStartTime(), 
                        availability.getEndTime(),
                        durationHours,
                        assignment.getPreferredTimeStart(),
                        assignment.getPreferredTimeEnd()
                );
                
                // Filter out conflicting slots
                for (TimeSlot slot : daySlots) {
                    if (!hasConflict(slot, teacher, existingEvents)) {
                        availableSlots.add(slot);
                    }
                }
            }
        }
        
        // Sort by preference (preferred times first)
        availableSlots.sort((slot1, slot2) -> {
            int score1 = calculateSlotScore(slot1, assignment);
            int score2 = calculateSlotScore(slot2, assignment);
            return Integer.compare(score2, score1); // Higher score first
        });
        
        return availableSlots;
    }
    
    private List<TimeSlot> generateTimeSlots(LocalDate date, 
                                           LocalTime startTime, 
                                           LocalTime endTime,
                                           int durationHours,
                                           LocalTime preferredStart,
                                           LocalTime preferredEnd) {
        
        List<TimeSlot> slots = new ArrayList<>();
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        
        // Sunday: No work
        if (dayOfWeek == 7) {
            return slots;
        }
        
        // Define university time slots
        List<TimeSlot> availableSlots = getUniversityTimeSlots(date, dayOfWeek);
        
        // Filter slots based on course duration
        for (TimeSlot universitySlot : availableSlots) {
            if (canFitCourse(universitySlot, durationHours)) {
                // Generate possible start times within this slot
                List<TimeSlot> possibleSlots = generateSlotsWithinTimeSlot(universitySlot, durationHours);
                slots.addAll(possibleSlots);
            }
        }
        
        return slots;
    }
    
    private List<TimeSlot> getUniversityTimeSlots(LocalDate date, int dayOfWeek) {
        List<TimeSlot> universitySlots = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(9, 0));
        
        // Morning slot: 9:00 AM - 12:15 PM (all days except Sunday)
        if (dayOfWeek != 7) {
            LocalDateTime morningStart = LocalDateTime.of(date, LocalTime.of(9, 0));
            LocalDateTime morningEnd = LocalDateTime.of(date, LocalTime.of(12, 15));
            universitySlots.add(new TimeSlot(morningStart, morningEnd));
        }
        
        // Afternoon slot: 1:30 PM - 4:45 PM (Monday, Tuesday, Thursday, Friday only)
        if (dayOfWeek != 3 && dayOfWeek != 6 && dayOfWeek != 7) { // Not Wednesday, Saturday, Sunday
            LocalDateTime afternoonStart = LocalDateTime.of(date, LocalTime.of(13, 30));
            LocalDateTime afternoonEnd = LocalDateTime.of(date, LocalTime.of(16, 45));
            universitySlots.add(new TimeSlot(afternoonStart, afternoonEnd));
        }
        
        return universitySlots;
    }
    
    private boolean canFitCourse(TimeSlot slot, int durationHours) {
        long slotDurationMinutes = java.time.Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
        long courseDurationMinutes = durationHours * 60;
        return slotDurationMinutes >= courseDurationMinutes;
    }
    
    private List<TimeSlot> generateSlotsWithinTimeSlot(TimeSlot universitySlot, int durationHours) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalDateTime current = universitySlot.getStartDateTime();
        LocalDateTime slotEnd = universitySlot.getEndDateTime();
        
        // Generate slots every 30 minutes within the time slot
        while (current.plusHours(durationHours).isBefore(slotEnd) || 
               current.plusHours(durationHours).equals(slotEnd)) {
            
            LocalDateTime courseStart = current;
            LocalDateTime courseEnd = current.plusHours(durationHours);
            
            slots.add(new TimeSlot(courseStart, courseEnd));
            current = current.plusMinutes(30); // Try every 30 minutes
        }
        
        return slots;
    }
    
    private boolean hasConflict(TimeSlot slot, User teacher, List<ScheduledEvent> existingEvents) {
        // Check against existing scheduled events
        for (ScheduledEvent event : existingEvents) {
            if (event.getTeacher().getId().equals(teacher.getId())) {
                if (timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(),
                                   event.getStartDateTime(), event.getEndDateTime())) {
                    return true;
                }
            }
        }
        
        // Check against existing events in database
        List<Event> existingDbEvents = eventRepository.findEventsByTeacherAndDateRange(
                teacher.getId(),
                slot.getStartDateTime().toLocalDate(),
                slot.getEndDateTime().toLocalDate()
        );
        
        for (Event event : existingDbEvents) {
            LocalDateTime eventStart = LocalDateTime.of(event.getDate(), event.getStartTime());
            LocalDateTime eventEnd = LocalDateTime.of(event.getDate(), event.getEndTime());
            if (timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(),
                               eventStart, eventEnd)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean timeSlotsOverlap(LocalDateTime start1, LocalDateTime end1,
                                   LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
    
    private Room findSuitableRoom(TimeSlot slot, Integer studentCount, String preferredRoomType) {
        List<Room> allRooms = roomRepository.findAll();
        
        for (Room room : allRooms) {
            // Check capacity
            if (studentCount != null && room.getCapacity() < studentCount) {
                continue;
            }
            
            // Check room type preference
            if (preferredRoomType != null && !room.getLocation().toLowerCase().contains(preferredRoomType.toLowerCase())) {
                // This is a soft preference, continue checking
            }
            
            // Check if room is available at this time
            if (isRoomAvailable(room, slot)) {
                return room;
            }
        }
        
        return null;
    }
    
    private boolean isRoomAvailable(Room room, TimeSlot slot) {
        List<Event> roomEvents = eventRepository.findEventsByRoomAndDateRange(
                room.getId(),
                slot.getStartDateTime().toLocalDate(),
                slot.getEndDateTime().toLocalDate()
        );
        
        for (Event event : roomEvents) {
            LocalDateTime eventStart = LocalDateTime.of(event.getDate(), event.getStartTime());
            LocalDateTime eventEnd = LocalDateTime.of(event.getDate(), event.getEndTime());
            if (timeSlotsOverlap(slot.getStartDateTime(), slot.getEndDateTime(),
                               eventStart, eventEnd)) {
                return false;
            }
        }
        
        return true;
    }
    
    private int calculateSlotScore(TimeSlot slot, TemplateCourseAssignment assignment) {
        int score = 0;
        LocalTime slotTime = slot.getStartDateTime().toLocalTime();
        int dayOfWeek = slot.getStartDateTime().getDayOfWeek().getValue();
        
        // Prefer slots within preferred time range
        if (assignment.getPreferredTimeStart() != null && assignment.getPreferredTimeEnd() != null) {
            if (slotTime.isAfter(assignment.getPreferredTimeStart()) && 
                slotTime.isBefore(assignment.getPreferredTimeEnd())) {
                score += 10;
            }
        }
        
        // Prefer certain days if specified
        if (assignment.getPreferredDays() != null) {
            String[] preferredDays = assignment.getPreferredDays().split(",");
            for (String day : preferredDays) {
                if (Integer.parseInt(day.trim()) == dayOfWeek) {
                    score += 5;
                    break;
                }
            }
        }
        
        // University time slot preferences
        // Prefer morning slots (9:00-12:15)
        if (slotTime.isAfter(LocalTime.of(8, 59)) && slotTime.isBefore(LocalTime.of(12, 16))) {
            score += 8;
        }
        
        // Afternoon slots (13:30-16:45) - slightly lower preference
        if (slotTime.isAfter(LocalTime.of(13, 29)) && slotTime.isBefore(LocalTime.of(16, 46))) {
            score += 6;
        }
        
        // Prefer weekdays over weekends
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            score += 3;
        }
        
        // Bonus for priority-based scheduling (higher priority courses get better slots)
        score += assignment.getPriority() * 2;
        
        return score;
    }
    
    private void saveScheduledEvent(ScheduledEvent scheduledEvent) {
        Event event = Event.builder()
                .title(scheduledEvent.getAssignment().getCourse().getName())
                .description("Auto-scheduled: " + scheduledEvent.getAssignment().getCourse().getSubject())
                .date(scheduledEvent.getStartDateTime().toLocalDate())
                .startTime(scheduledEvent.getStartDateTime().toLocalTime())
                .endTime(scheduledEvent.getEndDateTime().toLocalTime())
                .teacher(scheduledEvent.getTeacher())
                .room(scheduledEvent.getRoom())
                .type(com.reservation.model.enums.EventType.COURSE) // Default type
                .status(com.reservation.model.enums.EventStatus.SCHEDULED)
                .build();
        
        eventRepository.save(event);
    }
    
    // Inner classes for data transfer
    public static class SchedulingResult {
        private Long templateId;
        private int totalCourses = 0;
        private int scheduledCourses = 0;
        private int failedCourses = 0;
        private boolean success = false;
        private List<ScheduledEvent> scheduledEvents = new ArrayList<>();
        private List<SchedulingConflict> conflicts = new ArrayList<>();
        
        // Getters and setters
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        public int getTotalCourses() { return totalCourses; }
        public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
        public int getScheduledCourses() { return scheduledCourses; }
        public void setScheduledCourses(int scheduledCourses) { this.scheduledCourses = scheduledCourses; }
        public int getFailedCourses() { return failedCourses; }
        public void setFailedCourses(int failedCourses) { this.failedCourses = failedCourses; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public List<ScheduledEvent> getScheduledEvents() { return scheduledEvents; }
        public void setScheduledEvents(List<ScheduledEvent> scheduledEvents) { this.scheduledEvents = scheduledEvents; }
        public List<SchedulingConflict> getConflicts() { return conflicts; }
        public void setConflicts(List<SchedulingConflict> conflicts) { this.conflicts = conflicts; }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ScheduledEvent {
        private TemplateCourseAssignment assignment;
        private User teacher;
        private Room room;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SchedulingConflict {
        private TemplateCourseAssignment assignment;
        private String reason;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TimeSlot {
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
    }
}
