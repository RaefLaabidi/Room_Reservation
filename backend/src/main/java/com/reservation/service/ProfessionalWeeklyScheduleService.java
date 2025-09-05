package com.reservation.service;

import com.reservation.model.entity.*;
import com.reservation.repository.*;
import com.reservation.dto.response.EventResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.dto.response.RoomResponse;
import com.reservation.model.enums.EventType;
import com.reservation.model.enums.EventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessionalWeeklyScheduleService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    // University time constraints
    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 15);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 30);
    private static final LocalTime AFTERNOON_END = LocalTime.of(16, 45);
    private static final int SESSION_DURATION_MINUTES = 195; // 3h15min

    @Transactional
    public ProfessionalScheduleResult createOptimalWeeklySchedule(List<Long> courseIds) {
        log.info("Creating professional weekly schedule for {} courses", courseIds.size());
        
        LocalDate startOfWeek = getStartOfCurrentWeek();
        ProfessionalScheduleResult result = new ProfessionalScheduleResult();
        
        try {
            // Fetch courses
            List<Course> courses = courseRepository.findAllById(courseIds);
            if (courses.size() != courseIds.size()) {
                throw new RuntimeException("Some courses not found");
            }
            
            // Create scheduling grid for resource management
            SchedulingGrid grid = new SchedulingGrid();
            
            // Clear existing events for the week
            clearWeekEvents(startOfWeek);
            
            // Professional constraint-based scheduling
            List<EventResponse> scheduledEvents = new ArrayList<>();
            Set<String> scheduledCourseKeys = new HashSet<>();
            
            // Sort courses by priority (difficulty, credit hours, etc.)
            courses.sort(this::compareCoursesPriority);
            
            for (Course course : courses) {
                String courseKey = course.getId() + "-" + course.getName();
                if (scheduledCourseKeys.contains(courseKey)) {
                    log.warn("Course {} already scheduled, skipping duplicate", course.getName());
                    continue;
                }
                
                Optional<EventResponse> scheduled = findOptimalTimeSlot(course, grid, startOfWeek);
                if (scheduled.isPresent()) {
                    EventResponse event = scheduled.get();
                    scheduledEvents.add(event);
                    scheduledCourseKeys.add(courseKey);
                    
                    log.info("Successfully scheduled: {} on {}", 
                            course.getName(), event.getStartTime());
                } else {
                    log.warn("Could not find optimal slot for course: {}", course.getName());
                    result.getUnscheduledCourses().add(course.getName());
                }
            }
            
            result.setScheduledEvents(scheduledEvents);
            result.setMessage(String.format("Professional scheduling completed. Scheduled: %d/%d courses", 
                    scheduledEvents.size(), courses.size()));
            
            log.info("Professional scheduling completed: {}/{} courses scheduled", 
                    scheduledEvents.size(), courses.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error in professional scheduling", e);
            result.setMessage("Professional scheduling failed: " + e.getMessage());
            return result;
        }
    }

    private Optional<EventResponse> findOptimalTimeSlot(Course course, SchedulingGrid grid, LocalDate startOfWeek) {
        List<TimeSlotOption> options = new ArrayList<>();
        
        // Generate all possible time slots for the week
        for (DayOfWeek day : DayOfWeek.values()) {
            if (day == DayOfWeek.SUNDAY) continue; // No classes on Sunday
            
            LocalDate date = startOfWeek.with(day);
            
            // Morning slot
            if (day == DayOfWeek.WEDNESDAY || day == DayOfWeek.SATURDAY || 
                day == DayOfWeek.MONDAY || day == DayOfWeek.TUESDAY || 
                day == DayOfWeek.THURSDAY || day == DayOfWeek.FRIDAY) {
                
                LocalTime startTime = MORNING_START;
                if (grid.isSlotAvailable(day, startTime)) {
                    Optional<TimeSlotOption> option = createTimeSlotOption(course, day, startTime, date);
                    option.ifPresent(options::add);
                }
            }
            
            // Afternoon slot (not on Wednesday/Saturday)
            if (day != DayOfWeek.WEDNESDAY && day != DayOfWeek.SATURDAY) {
                LocalTime startTime = AFTERNOON_START;
                if (grid.isSlotAvailable(day, startTime)) {
                    Optional<TimeSlotOption> option = createTimeSlotOption(course, day, startTime, date);
                    option.ifPresent(options::add);
                }
            }
        }
        
        if (options.isEmpty()) {
            log.warn("No available time slots for course: {}", course.getName());
            return Optional.empty();
        }
        
        // Find the best option based on scoring
        TimeSlotOption bestOption = options.stream()
                .max(Comparator.comparingDouble(TimeSlotOption::getScore))
                .orElse(null);
        
        if (bestOption != null) {
            return Optional.of(createEventResponse(bestOption, startOfWeek));
        }
        
        return Optional.empty();
    }

    private Optional<TimeSlotOption> createTimeSlotOption(Course course, DayOfWeek day, LocalTime startTime, LocalDate date) {
        // Find suitable teachers for this course
        List<TeacherSubject> qualifiedTeachers = teacherSubjectRepository.findBySubject(course.getSubject());
        if (qualifiedTeachers.isEmpty()) {
            log.warn("No qualified teachers found for subject: {}", course.getSubject());
            return Optional.empty();
        }
        
        // Find available rooms
        List<Room> availableRooms = roomRepository.findAll().stream()
                .filter(room -> room.getCapacity() >= 30) // Minimum capacity
                .collect(Collectors.toList());
        
        if (availableRooms.isEmpty()) {
            log.warn("No available rooms found");
            return Optional.empty();
        }
        
        // Select best teacher (highest expertise)
        User teacher = qualifiedTeachers.stream()
                .sorted((a, b) -> Double.compare(b.getExpertiseLevel(), a.getExpertiseLevel()))
                .map(ts -> userRepository.findById(ts.getTeacher().getId()).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        
        if (teacher == null) {
            return Optional.empty();
        }
        
        // Select best room (optimal capacity)
        Room room = availableRooms.stream()
                .sorted(Comparator.comparingInt(Room::getCapacity))
                .findFirst()
                .orElse(null);
        
        if (room == null) {
            return Optional.empty();
        }
        
        // Calculate score for this option
        double score = calculateOptionScore(teacher, room, day, startTime);
        
        return Optional.of(TimeSlotOption.builder()
                .course(course)
                .teacher(teacher)
                .room(room)
                .dayOfWeek(day)
                .startTime(startTime)
                .date(date)
                .score(score)
                .build());
    }

    private double calculateOptionScore(User teacher, Room room, DayOfWeek day, LocalTime startTime) {
        double score = 0.0;
        
        // Morning sessions are generally preferred
        if (startTime.equals(MORNING_START)) {
            score += 10.0;
        }
        
        // Monday-Friday preferred over Saturday
        if (day != DayOfWeek.SATURDAY) {
            score += 5.0;
        }
        
        // Room capacity optimization (not too big, not too small)
        if (room.getCapacity() >= 30 && room.getCapacity() <= 50) {
            score += 8.0;
        }
        
        // Add some randomness to avoid always picking the same options
        score += Math.random() * 3.0;
        
        return score;
    }

    private EventResponse createEventResponse(TimeSlotOption option, LocalDate startOfWeek) {
        LocalTime endTime = option.getStartTime().plusMinutes(SESSION_DURATION_MINUTES);
        
        // Save to database
        Event event = new Event();
        event.setTitle(option.getCourse().getName());
        event.setDescription("Auto-scheduled: " + option.getCourse().getName());
        event.setDate(option.getDate());
        event.setStartTime(option.getStartTime());
        event.setEndTime(endTime);
        event.setTeacher(option.getTeacher());
        event.setRoom(option.getRoom());
        event.setCourse(option.getCourse());
        event.setType(EventType.COURSE);
        event.setStatus(EventStatus.SCHEDULED);
        
        Event savedEvent = eventRepository.save(event);
        
        return EventResponse.builder()
                .id(savedEvent.getId())
                .title(savedEvent.getTitle())
                .description(savedEvent.getDescription())
                .startTime(savedEvent.getStartTime())
                .endTime(savedEvent.getEndTime())
                .teacher(UserResponse.builder()
                        .id(option.getTeacher().getId())
                        .name(option.getTeacher().getName())
                        .email(option.getTeacher().getEmail())
                        .role(option.getTeacher().getRole())
                        .build())
                .room(RoomResponse.builder()
                        .id(option.getRoom().getId())
                        .name(option.getRoom().getName())
                        .capacity(option.getRoom().getCapacity())
                        .location(option.getRoom().getLocation())
                        .build())
                .build();
    }

    private int compareCoursesPriority(Course a, Course b) {
        // Sort by name for consistency
        return a.getName().compareTo(b.getName());
    }

    private LocalDate getStartOfCurrentWeek() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void clearWeekEvents(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        List<Event> existingEvents = eventRepository.findAll().stream()
                .filter(event -> {
                    LocalDate eventDate = event.getDate();
                    return !eventDate.isBefore(startOfWeek) && !eventDate.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
        
        if (!existingEvents.isEmpty()) {
            eventRepository.deleteAll(existingEvents);
            log.info("Cleared {} existing events for the week", existingEvents.size());
        }
    }

    // Supporting classes
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSlotOption {
        private Course course;
        private User teacher;
        private Room room;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalDate date;
        private double score;
    }
    
    public static class SchedulingGrid {
        private final Map<String, Set<String>> occupiedSlots = new HashMap<>();
        
        public boolean isSlotAvailable(DayOfWeek day, LocalTime startTime) {
            String key = day + "-" + startTime;
            return !occupiedSlots.containsKey(key) || occupiedSlots.get(key).isEmpty();
        }
        
        public void markSlotUsed(DayOfWeek day, LocalTime startTime, Long teacherId, Long roomId) {
            String key = day + "-" + startTime;
            Set<String> resources = occupiedSlots.computeIfAbsent(key, k -> new HashSet<>());
            resources.add("teacher-" + teacherId);
            resources.add("room-" + roomId);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfessionalScheduleResult {
        @Builder.Default
        private List<EventResponse> scheduledEvents = new ArrayList<>();
        @Builder.Default
        private List<String> unscheduledCourses = new ArrayList<>();
        private String message;
        @Builder.Default
        private boolean success = true;
    }
}
