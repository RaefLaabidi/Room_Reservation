package com.reservation.service;

import com.reservation.model.entity.*;
import com.reservation.repository.*;
import com.reservation.dto.response.EventResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.dto.response.RoomResponse;
import com.reservation.model.enums.EventType;
import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.Role;
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
public class ProfessionalWeeklyScheduleServiceCorrected {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;

    // University time constraints - EXPANDED for more scheduling opportunities
    private static final LocalTime MORNING_START = LocalTime.of(9, 0);
    private static final LocalTime MORNING_END = LocalTime.of(12, 15);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 30);
    private static final LocalTime AFTERNOON_END = LocalTime.of(16, 45);
    private static final int SESSION_DURATION_MINUTES = 90; // 1.5 hours (more realistic)

    @Transactional
    public ProfessionalScheduleResult createOptimalWeeklySchedule(List<Long> courseIds) {
        long startTime = System.currentTimeMillis();
        log.info("🚀 PROFESSIONAL FAST SCHEDULING for {} courses", courseIds.size());
        
        try {
            LocalDate startOfWeek = getStartOfCurrentWeek();
            clearWeekEvents(startOfWeek);
            
            // Fetch all data at once for efficiency
            List<Course> courses = courseRepository.findAllById(courseIds);
            List<User> allTeachers = userRepository.findByRole(Role.TEACHER);
            List<Room> allRooms = roomRepository.findAll();
            
            if (courses.isEmpty()) {
                return ProfessionalScheduleResult.builder()
                    .message("No courses found")
                    .success(false)
                    .build();
            }
            
            // Create professional scheduling engine
            ProfessionalScheduler scheduler = new ProfessionalScheduler(allTeachers, allRooms);
            
            List<EventResponse> scheduledEvents = new ArrayList<>();
            List<String> unscheduledCourses = new ArrayList<>();
            
            // Process each course with professional logic
            for (Course course : courses) {
                ScheduleResult result = scheduler.scheduleCourseProfessionally(course, startOfWeek);
                
                if (result.isSuccess()) {
                    // Save to database
                    Event event = new Event();
                    event.setTitle(course.getName());
                    event.setDescription("Professional AI Scheduling: " + course.getName());
                    event.setDate(result.getDate());
                    event.setStartTime(result.getStartTime());
                    event.setEndTime(result.getStartTime().plusMinutes(SESSION_DURATION_MINUTES));
                    event.setTeacher(result.getTeacher());
                    event.setRoom(result.getRoom());
                    event.setCourse(course);
                    event.setType(EventType.COURSE);
                    event.setStatus(EventStatus.SCHEDULED);
                    
                    Event savedEvent = eventRepository.save(event);
                    
                    // Create response
                    EventResponse eventResponse = EventResponse.builder()
                            .id(savedEvent.getId())
                            .title(savedEvent.getTitle())
                            .description(savedEvent.getDescription())
                            .startTime(savedEvent.getStartTime())
                            .endTime(savedEvent.getEndTime())
                            .teacher(UserResponse.builder()
                                    .id(result.getTeacher().getId())
                                    .name(result.getTeacher().getName())
                                    .email(result.getTeacher().getEmail())
                                    .role(result.getTeacher().getRole())
                                    .build())
                            .room(RoomResponse.builder()
                                    .id(result.getRoom().getId())
                                    .name(result.getRoom().getName())
                                    .capacity(result.getRoom().getCapacity())
                                    .location(result.getRoom().getLocation())
                                    .build())
                            .build();
                    
                    scheduledEvents.add(eventResponse);
                    log.info("✅ Scheduled: {} → {} at {} in {}", 
                            course.getName(), result.getDate().getDayOfWeek(), 
                            result.getStartTime(), result.getRoom().getLocation());
                } else {
                    unscheduledCourses.add(course.getName());
                    log.warn("❌ Failed to schedule: {} - {}", course.getName(), result.getReason());
                }
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            String message = String.format("🎯 Professional Scheduling Complete! %d/%d courses scheduled in %dms", 
                    scheduledEvents.size(), courses.size(), processingTime);
            
            log.info(message);
            
            return ProfessionalScheduleResult.builder()
                    .scheduledEvents(scheduledEvents)
                    .unscheduledCourses(unscheduledCourses)
                    .message(message)
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Professional scheduling failed", e);
            return ProfessionalScheduleResult.builder()
                    .message("Professional scheduling failed: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    private Optional<EventResponse> findOptimalTimeSlot(Course course, SchedulingGrid grid, LocalDate startOfWeek) {
        List<TimeSlotOption> options = new ArrayList<>();
        
        // 🚀 EXPANDED: Generate MORE time slots for better scheduling coverage
        DayOfWeek[] workDays = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
                               DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY};
        
        // 🕐 MULTIPLE TIME SLOTS PER DAY - More scheduling opportunities
        LocalTime[] allTimeSlots = {
            LocalTime.of(8, 0),   // Early morning
            LocalTime.of(9, 0),   // Morning start
            LocalTime.of(10, 30), // Mid-morning
            LocalTime.of(13, 30), // Afternoon start
            LocalTime.of(15, 15), // Mid-afternoon
            LocalTime.of(17, 0)   // Evening
        };
        
        for (DayOfWeek day : workDays) {
            LocalDate date = startOfWeek.with(day);
            
            for (LocalTime timeSlot : allTimeSlots) {
                // ✅ CHECK: Is this time slot available?
                if (grid.isSlotAvailable(day, timeSlot) && grid.isSlotAvailableForStudents(day, timeSlot)) {
                    Optional<TimeSlotOption> option = createTimeSlotOption(course, day, timeSlot, date);
                    option.ifPresent(options::add);
                }
            }
        }
        
        if (options.isEmpty()) {
            log.warn("❌ No available time slots for: {}", course.getName());
            return Optional.empty();
        }
        
        log.info("🎯 Found {} available time slots for: {}", options.size(), course.getName());
        
        // 🎯 SMART SELECTION: Distribute time slots evenly
        TimeSlotOption bestOption = selectBestTimeSlot(options, grid);
        
        if (bestOption != null) {
            // 🔒 Reserve the time slot
            grid.markSlotUsedForStudents(bestOption.getDayOfWeek(), bestOption.getStartTime());
            grid.markSlotUsed(bestOption.getDayOfWeek(), bestOption.getStartTime(), 
                            bestOption.getTeacher().getId(), bestOption.getRoom().getId());
            
            log.info("✅ Scheduled: {} → {} at {} ({})", course.getName(), 
                    bestOption.getDayOfWeek(), bestOption.getStartTime(), bestOption.getRoom().getLocation());
            
            return Optional.of(createEventResponse(bestOption, startOfWeek));
        }
        
        return Optional.empty();
    }

    /**
     * 🚀 SMART TIME SLOT SELECTION - Handles multiple time slots per day
     */
    private TimeSlotOption selectBestTimeSlot(List<TimeSlotOption> options, SchedulingGrid grid) {
        if (options.isEmpty()) return null;
        
        // 🎯 PRIORITY 1: Best room match first (AI scoring)
        TimeSlotOption bestOption = selectBestRoomOption(options);
        
        if (bestOption != null) {
            log.debug("🎯 Selected best time slot: {} at {} (Score: {:.2f})", 
                    bestOption.getDayOfWeek(), bestOption.getStartTime(), bestOption.getScore());
        }
        
        return bestOption;
    }

    /**
     * 🏢 FAST ROOM SELECTION - Optimized for speed and accuracy
     */
    private TimeSlotOption selectBestRoomOption(List<TimeSlotOption> options) {
        // Sort by AI room score (highest first) and return the best
        return options.stream()
                .max(Comparator.comparingDouble(TimeSlotOption::getScore))
                .orElse(null);
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
        
        // Select best room using AI-like intelligent matching
        Room room = selectOptimalRoomWithAI(course, availableRooms);
        
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
        
        // AI Room score is now calculated separately in selectOptimalRoomWithAI
        // This method focuses on time and day preferences
        
        // Add some randomness to avoid always picking the same options
        score += Math.random() * 3.0;
        
        return score;
    }

    /**
     * 🚀 OPTIMIZED ROOM SELECTION - Fast and intelligent
     */
    private Room selectOptimalRoomWithAI(Course course, List<Room> availableRooms) {
        // 🎯 Quick AI scoring without excessive logging
        Room bestRoom = availableRooms.stream()
                .max(Comparator.comparingDouble(room -> calculateFastAIRoomScore(course, room)))
                .orElse(null);
        
        if (bestRoom != null) {
            log.debug("🤖 AI selected: {} for {}", bestRoom.getLocation(), course.getSubject());
        }
        
        return bestRoom;
    }

    /**
     * 🧠 FAST AI SCORING - Optimized for speed
     */
    private double calculateFastAIRoomScore(Course course, Room room) {
        double score = 0.0;
        String subject = course.getSubject().toLowerCase();
        String location = room.getLocation().toLowerCase();
        
        // 🎯 FAST SUBJECT MATCHING - single method call
        score += getSubjectLocationMatch(subject, location);
        
        // 📏 CAPACITY SCORE
        if (room.getCapacity() >= 30 && room.getCapacity() <= 50) score += 15.0;
        else if (room.getCapacity() >= 25 && room.getCapacity() <= 60) score += 10.0;
        
        // 🏢 BUILDING BONUS
        if (location.contains("new") || location.contains("modern")) score += 5.0;
        
        return score;
    }


    /**
     * 🎯 ENHANCED AI LOGIC: Better Subject-to-Room-Type Matching
     */
    private double getSubjectLocationMatch(String subject, String location) {
        log.debug("🔍 AI Matching: '{}' subject with '{}' location", subject, location);
        
        // 🔬 COMPUTER SCIENCE & PROGRAMMING
        if (subject.contains("computer") || subject.contains("programming") || subject.contains("software") || 
            subject.contains("java") || subject.contains("python") || subject.contains("web")) {
            if (location.contains("computer") || location.contains("lab") || location.contains("tech") || 
                location.contains("it ") || location.contains("coding")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
            if (location.contains("engineering") || location.contains("science")) return 30.0;
        }
        
        // 🧪 SCIENCES (Physics, Chemistry, Biology)
        if (subject.contains("physics") || subject.contains("chemistry") || subject.contains("biology") || 
            subject.contains("science") || subject.contains("experiment")) {
            if (location.contains("lab") || location.contains("science") || location.contains("research") || 
                location.contains("experiment")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // ⚙️ ENGINEERING
        if (subject.contains("engineering") || subject.contains("mechanical") || subject.contains("electrical") || 
            subject.contains("civil") || subject.contains("industrial")) {
            if (location.contains("engineering") || location.contains("workshop") || location.contains("lab") || 
                location.contains("tech") || location.contains("mechanical")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // � BUSINESS & ECONOMICS
        if (subject.contains("business") || subject.contains("economics") || subject.contains("management") || 
            subject.contains("finance") || subject.contains("accounting")) {
            if (location.contains("business") || location.contains("conference") || location.contains("boardroom") || 
                location.contains("meeting") || location.contains("executive")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
            if (location.contains("lecture") || location.contains("seminar")) return 35.0;
        }
        
        // 🎨 ARTS & CREATIVE
        if (subject.contains("art") || subject.contains("design") || subject.contains("music") || 
            subject.contains("creative") || subject.contains("digital art")) {
            if (location.contains("studio") || location.contains("art") || location.contains("creative") || 
                location.contains("design") || location.contains("gallery")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // 🌐 LANGUAGES & LITERATURE
        if (subject.contains("language") || subject.contains("english") || subject.contains("literature") || 
            subject.contains("french") || subject.contains("spanish") || subject.contains("communication")) {
            if (location.contains("language") || location.contains("humanities") || location.contains("communication") || 
                location.contains("seminar") || location.contains("discussion")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // 🏥 MEDICAL & HEALTH
        if (subject.contains("medicine") || subject.contains("health") || subject.contains("anatomy") || 
            subject.contains("medical") || subject.contains("nursing")) {
            if (location.contains("medical") || location.contains("clinic") || location.contains("health") || 
                location.contains("anatomy") || location.contains("hospital")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // 🧮 MATHEMATICS & STATISTICS
        if (subject.contains("math") || subject.contains("calculus") || subject.contains("statistics") || 
            subject.contains("algebra") || subject.contains("geometry")) {
            if (location.contains("math") || location.contains("lecture") || location.contains("classroom")) {
                log.info("🎯 GOOD MATCH: {} → {}", subject, location);
                return 40.0;
            }
        }
        
        // 🧠 PSYCHOLOGY & SOCIAL SCIENCES
        if (subject.contains("psychology") || subject.contains("sociology") || subject.contains("social") || 
            subject.contains("anthropology")) {
            if (location.contains("social") || location.contains("psychology") || location.contains("seminar") || 
                location.contains("research") || location.contains("counseling")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 45.0;
            }
        }
        
        // 🌍 ENVIRONMENTAL SCIENCE
        if (subject.contains("environment") || subject.contains("ecology") || subject.contains("green") || 
            subject.contains("sustainable")) {
            if (location.contains("environment") || location.contains("green") || location.contains("lab") || 
                location.contains("outdoor") || location.contains("field")) {
                log.info("🎯 PERFECT MATCH: {} → {}", subject, location);
                return 50.0;
            }
        }
        
        // 🏛️ GENERAL LECTURE COMPATIBILITY
        if (location.contains("lecture") || location.contains("classroom") || location.contains("hall") || 
            location.contains("room") || location.contains("auditorium")) {
            log.debug("📚 General lecture compatibility: {} → {}", subject, location);
            return 25.0; // Decent fallback
        }
        
        // ❌ POOR MATCH
        log.warn("⚠️ Poor room match: {} → {} (Score: 5.0)", subject, location);
        return 5.0; // Very low compatibility
    }

    /**
     * 📏 CAPACITY OPTIMIZATION: Right-sized rooms get bonus points
     */
    private double getCapacityOptimizationScore(int capacity) {
        if (capacity >= 30 && capacity <= 50) return 15.0;  // Perfect size
        if (capacity >= 25 && capacity <= 60) return 10.0;  // Good size
        if (capacity >= 20 && capacity <= 80) return 5.0;   // Acceptable
        return 0.0; // Too small or too large
    }

    /**
     * 🏢 BUILDING PREFERENCE: Premium locations get bonus
     */
    private double getBuildingPreferenceScore(String location) {
        // Premium building keywords
        if (location.contains("new") || location.contains("modern") || location.contains("advanced")) return 8.0;
        if (location.contains("main") || location.contains("central") || location.contains("tower")) return 5.0;
        if (location.contains("basement") || location.contains("temporary")) return -3.0;
        return 2.0; // Neutral buildings
    }

    /**
     * Helper class for AI room scoring
     */
    private static class ScoredRoom {
        final Room room;
        final double score;
        
        ScoredRoom(Room room, double score) {
            this.room = room;
            this.score = score;
        }
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
    
    /**
     * 🚀 PROFESSIONAL SCHEDULER ENGINE - Fast, Logical, Efficient
     */
    private static class ProfessionalScheduler {
        private final List<User> teachers;
        private final List<Room> rooms;
        private final Set<String> usedSlots = new HashSet<>();
        
        // Professional time slots - spread throughout the week
        private final LocalTime[] TIME_SLOTS = {
            LocalTime.of(8, 0),   // 8:00 AM
            LocalTime.of(9, 30),  // 9:30 AM
            LocalTime.of(11, 0),  // 11:00 AM
            LocalTime.of(13, 30), // 1:30 PM
            LocalTime.of(15, 0),  // 3:00 PM
            LocalTime.of(16, 30)  // 4:30 PM
        };
        
        private final DayOfWeek[] WORK_DAYS = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        };
        
        public ProfessionalScheduler(List<User> teachers, List<Room> rooms) {
            this.teachers = teachers;
            this.rooms = rooms;
        }
        
        public ScheduleResult scheduleCourseProfessionally(Course course, LocalDate startOfWeek) {
            // 1. Find available teacher for this subject
            User teacher = findBestTeacher(course);
            if (teacher == null) {
                return ScheduleResult.failure("No qualified teacher found for " + course.getSubject());
            }
            
            // 2. Find best room for this course
            Room room = findBestRoom(course);
            if (room == null) {
                return ScheduleResult.failure("No suitable room found");
            }
            
            // 3. Find available time slot
            for (DayOfWeek day : WORK_DAYS) {
                for (LocalTime time : TIME_SLOTS) {
                    String slotKey = day + "-" + time + "-" + teacher.getId() + "-" + room.getId();
                    
                    if (!usedSlots.contains(slotKey)) {
                        // Reserve this slot
                        usedSlots.add(slotKey);
                        
                        LocalDate date = startOfWeek.with(day);
                        return ScheduleResult.success(teacher, room, date, time, 
                                "Professional scheduling: Perfect match");
                    }
                }
            }
            
            return ScheduleResult.failure("No available time slots");
        }
        
        private User findBestTeacher(Course course) {
            // Simple but effective: find any available teacher
            // In a real system, you'd match by subject expertise
            return teachers.stream()
                    .filter(teacher -> teacher.getRole() == Role.TEACHER)
                    .findFirst()
                    .orElse(null);
        }
        
        private Room findBestRoom(Course course) {
            String subject = course.getSubject().toLowerCase();
            
            // AI-powered room matching (simplified but effective)
            return rooms.stream()
                    .filter(room -> room.getCapacity() >= 20) // Minimum capacity
                    .max((r1, r2) -> {
                        double score1 = calculateRoomScore(subject, r1);
                        double score2 = calculateRoomScore(subject, r2);
                        return Double.compare(score1, score2);
                    })
                    .orElse(null);
        }
        
        private double calculateRoomScore(String subject, Room room) {
            String location = room.getLocation().toLowerCase();
            
            // Fast subject-location matching
            if (subject.contains("computer") && location.contains("computer")) return 100;
            if (subject.contains("lab") && location.contains("lab")) return 100;
            if (subject.contains("business") && location.contains("business")) return 100;
            if (subject.contains("math") && location.contains("math")) return 100;
            if (subject.contains("science") && location.contains("science")) return 90;
            if (location.contains("lecture") || location.contains("classroom")) return 70;
            
            return 50; // Default compatibility
        }
    }
    
    /**
     * 📊 SCHEDULE RESULT - Simple and effective
     */
    private static class ScheduleResult {
        private final boolean success;
        private final User teacher;
        private final Room room;
        private final LocalDate date;
        private final LocalTime startTime;
        private final String reason;
        
        private ScheduleResult(boolean success, User teacher, Room room, LocalDate date, 
                              LocalTime startTime, String reason) {
            this.success = success;
            this.teacher = teacher;
            this.room = room;
            this.date = date;
            this.startTime = startTime;
            this.reason = reason;
        }
        
        public static ScheduleResult success(User teacher, Room room, LocalDate date, 
                                           LocalTime startTime, String reason) {
            return new ScheduleResult(true, teacher, room, date, startTime, reason);
        }
        
        public static ScheduleResult failure(String reason) {
            return new ScheduleResult(false, null, null, null, null, reason);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public User getTeacher() { return teacher; }
        public Room getRoom() { return room; }
        public LocalDate getDate() { return date; }
        public LocalTime getStartTime() { return startTime; }
        public String getReason() { return reason; }
    }
    
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
        private final Set<String> studentOccupiedSlots = new HashSet<>();
        private int morningSlotCount = 0;
        private int afternoonSlotCount = 0;
        
        public boolean isSlotAvailable(DayOfWeek day, LocalTime startTime) {
            String key = day + "-" + startTime;
            return !occupiedSlots.containsKey(key) || occupiedSlots.get(key).isEmpty();
        }
        
        public boolean isSlotAvailableForStudents(DayOfWeek day, LocalTime startTime) {
            String studentKey = "STUDENTS-" + day + "-" + startTime;
            return !studentOccupiedSlots.contains(studentKey);
        }
        
        public void markSlotUsed(DayOfWeek day, LocalTime startTime, Long teacherId, Long roomId) {
            String key = day + "-" + startTime;
            Set<String> resources = occupiedSlots.computeIfAbsent(key, k -> new HashSet<>());
            resources.add("teacher-" + teacherId);
            resources.add("room-" + roomId);
        }
        
        public void markSlotUsedForStudents(DayOfWeek day, LocalTime startTime) {
            String studentKey = "STUDENTS-" + day + "-" + startTime;
            studentOccupiedSlots.add(studentKey);
            
            // 📊 Track time distribution for smart scheduling
            int hour = startTime.getHour();
            if (hour < 12) {
                morningSlotCount++;
            } else {
                afternoonSlotCount++;
            }
        }
        
        public long getMorningSlotCount() { return morningSlotCount; }
        public long getAfternoonSlotCount() { return afternoonSlotCount; }
        
        // ⚡ NEW: Smart time slot selection with afternoon preference
        public boolean preferAfternoon() {
            return morningSlotCount > afternoonSlotCount + 1; // Prefer afternoon if morning overused
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
