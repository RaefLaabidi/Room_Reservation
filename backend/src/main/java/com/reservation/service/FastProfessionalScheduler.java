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
public class FastProfessionalScheduler {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;

    // Simple and effective scheduling
    private static final int SESSION_DURATION_MINUTES = 90; // 1.5 hours

    @Transactional
    public ProfessionalScheduleResult scheduleCoursesOptimally(List<Long> courseIds) {
        long startTime = System.currentTimeMillis();
        log.info("🚀 PROFESSIONAL SCHEDULING - {} courses", courseIds.size());

        try {
            // 🚀 PERFORMANCE: Batch fetch all data once
            LocalDate startOfWeek = getStartOfCurrentWeek();
            clearWeekEvents(startOfWeek);

            List<Course> courses = courseRepository.findAllById(courseIds);
            List<User> teachers = userRepository.findByRole(Role.TEACHER);
            List<Room> rooms = roomRepository.findAll();

            // Early validation
            if (courses.isEmpty()) return createFailResult("No courses found");
            if (teachers.isEmpty()) return createFailResult("No teachers available");
            if (rooms.isEmpty()) return createFailResult("No rooms available");

            // 🎯 SMART: Pre-sort courses by priority (difficult subjects first)
            courses.sort(this::compareCoursesByPriority);

            // Professional time grid with optimization
            List<TimeSlot> availableSlots = generateAllTimeSlots(startOfWeek);
            Set<String> usedSlots = new HashSet<>();
            Map<DayOfWeek, Integer> dailyCourseCount = new HashMap<>();
            
            // 🚀 CACHE: Pre-calculate room scores for faster selection
            Map<String, Double> roomScoreCache = new HashMap<>();

            List<EventResponse> scheduledEvents = new ArrayList<>();
            List<String> unscheduledCourses = new ArrayList<>();

            // 🎯 SMART SCHEDULING: Process courses with intelligent logic
            for (Course course : courses) {
                boolean scheduled = false;

                // Try slots in priority order
                for (TimeSlot slot : availableSlots) {
                    String slotKey = slot.getDay() + "-" + slot.getTime();
                    
                    // Check daily limit (max 2 courses per day)
                    int currentDayCount = dailyCourseCount.getOrDefault(slot.getDay(), 0);
                    if (currentDayCount >= 2) continue;
                    
                    if (!usedSlots.contains(slotKey)) {
                        // 🚀 OPTIMIZED: Smart teacher and room selection
                        User teacher = selectBestTeacher(teachers, course);
                        Room room = selectBestRoomCached(rooms, course, roomScoreCache);

                        if (teacher != null && room != null) {
                            // Create and save event
                            Event event = createEvent(course, teacher, room, slot);
                            Event savedEvent = eventRepository.save(event);

                            scheduledEvents.add(createEventResponse(savedEvent));
                            usedSlots.add(slotKey);
                            dailyCourseCount.put(slot.getDay(), currentDayCount + 1);
                            scheduled = true;

                            log.info("✅ SMART: {} → {} at {} in {} (Priority: {})", 
                                course.getName(), slot.getDay(), slot.getTime(), 
                                room.getLocation(), getCoursePriority(course));
                            break;
                        }
                    }
                }

                if (!scheduled) {
                    unscheduledCourses.add(course.getName());
                    log.warn("❌ Could not schedule: {} (may need more time slots)", course.getName());
                }
            }

            long processingTime = System.currentTimeMillis() - startTime;
            
            // Professional result summary
            String distribution = getDayDistributionSummary(dailyCourseCount);
            String message = String.format("🎯 PROFESSIONAL Scheduling: %d/%d courses in %dms. %s", 
                scheduledEvents.size(), courses.size(), processingTime, distribution);

            log.info("🏆 " + message);

            return ProfessionalScheduleResult.builder()
                .scheduledEvents(scheduledEvents)
                .unscheduledCourses(unscheduledCourses)
                .message(message)
                .success(true)
                .build();

        } catch (Exception e) {
            log.error("❌ Professional scheduling failed", e);
            return createFailResult("Professional scheduling error: " + e.getMessage());
        }
    }
    
    private Room selectBestRoomCached(List<Room> rooms, Course course, Map<String, Double> cache) {
        String subject = course.getSubject().toLowerCase();
        
        return rooms.stream()
            .filter(room -> room.getCapacity() >= 15)
            .max((r1, r2) -> {
                String key1 = subject + "-" + r1.getId();
                String key2 = subject + "-" + r2.getId();
                
                double score1 = cache.computeIfAbsent(key1, k -> calculateAdvancedRoomScore(subject, r1));
                double score2 = cache.computeIfAbsent(key2, k -> calculateAdvancedRoomScore(subject, r2));
                
                return Double.compare(score1, score2);
            })
            .orElse(null);
    }
    
    private int compareCoursesByPriority(Course a, Course b) {
        // Prioritize difficult/specialized subjects first
        int priorityA = getCourseSchedulingPriority(a);
        int priorityB = getCourseSchedulingPriority(b);
        return Integer.compare(priorityB, priorityA); // Higher priority first
    }
    
    private String getCoursePriority(Course course) {
        return "P" + getCourseSchedulingPriority(course);
    }
    
    private int getCourseSchedulingPriority(Course course) {
        String subject = course.getSubject().toLowerCase();
        
        // High priority (need special rooms/equipment)
        if (subject.contains("lab") || subject.contains("computer") || subject.contains("science")) return 5;
        if (subject.contains("art") || subject.contains("studio") || subject.contains("workshop")) return 4;
        
        // Medium priority
        if (subject.contains("business") || subject.contains("engineering")) return 3;
        if (subject.contains("math") || subject.contains("language")) return 2;
        
        return 1; // Default priority
    }
    
    private String getDayDistributionSummary(Map<DayOfWeek, Integer> dailyCount) {
        return "Distribution: " + dailyCount.entrySet().stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> entry.getKey().name().substring(0, 3) + ":" + entry.getValue())
            .collect(Collectors.joining(", "));
    }

    private List<TimeSlot> generateAllTimeSlots(LocalDate startOfWeek) {
        List<TimeSlot> slots = new ArrayList<>();

        // 🎯 PROFESSIONAL UNIVERSITY TIME SLOTS (Prioritized by preference)
        LocalTime[] priorityTimes = {
            LocalTime.of(9, 0),    // 9:00 AM - Prime morning slot
            LocalTime.of(14, 0),   // 2:00 PM - Prime afternoon slot
            LocalTime.of(10, 30),  // 10:30 AM - Good morning slot
            LocalTime.of(15, 30),  // 3:30 PM - Good afternoon slot
        };

        // Work days (prioritized)
        DayOfWeek[] priorityDays = {
            DayOfWeek.TUESDAY,     // Best day (students alert, no Monday blues)
            DayOfWeek.THURSDAY,    // Second best day
            DayOfWeek.MONDAY,      // Start of week
            DayOfWeek.WEDNESDAY,   // Mid-week
            DayOfWeek.FRIDAY,      // End of week
            DayOfWeek.SATURDAY     // Weekend (last resort)
        };

        // Generate slots in priority order for better scheduling
        for (DayOfWeek day : priorityDays) {
            LocalDate date = startOfWeek.with(day);
            
            for (LocalTime time : priorityTimes) {
                // Skip Saturday afternoon (not professional)
                if (day == DayOfWeek.SATURDAY && time.getHour() >= 14) {
                    continue;
                }
                
                slots.add(new TimeSlot(day, time, date));
            }
        }

        log.info("📅 Generated {} professional time slots (prioritized)", slots.size());
        return slots;
    }

    private User selectBestTeacher(List<User> teachers, Course course) {
        String courseSubject = course.getSubject().toLowerCase();
        
        // 🎯 PROFESSIONAL: Match teacher expertise to course subject
        for (User teacher : teachers) {
            if (teacher.getRole() == Role.TEACHER) {
                String teacherName = teacher.getName().toLowerCase();
                String teacherEmail = teacher.getEmail().toLowerCase();
                
                // Smart subject matching based on teacher profile
                if (isTeacherQualifiedForSubject(teacherName, teacherEmail, courseSubject)) {
                    log.debug("🎓 Perfect teacher match: {} for {}", teacher.getName(), course.getSubject());
                    return teacher;
                }
            }
        }
        
        // Fallback: return any available teacher
        return teachers.stream()
            .filter(t -> t.getRole() == Role.TEACHER)
            .findFirst()
            .orElse(null);
    }
    
    private boolean isTeacherQualifiedForSubject(String teacherName, String teacherEmail, String subject) {
        // Professional teacher-subject matching logic
        if (subject.contains("computer") || subject.contains("programming")) {
            return teacherName.contains("tech") || teacherEmail.contains("cs") || 
                   teacherName.contains("dev") || teacherEmail.contains("computer");
        }
        if (subject.contains("math") || subject.contains("algebra")) {
            return teacherName.contains("math") || teacherEmail.contains("math") ||
                   teacherName.contains("calc") || teacherEmail.contains("statistics");
        }
        if (subject.contains("business") || subject.contains("economics")) {
            return teacherName.contains("business") || teacherEmail.contains("business") ||
                   teacherName.contains("econ") || teacherEmail.contains("management");
        }
        if (subject.contains("science") || subject.contains("physics") || subject.contains("chemistry")) {
            return teacherName.contains("science") || teacherEmail.contains("science") ||
                   teacherName.contains("lab") || teacherEmail.contains("research");
        }
        if (subject.contains("language") || subject.contains("english") || subject.contains("literature")) {
            return teacherName.contains("lang") || teacherEmail.contains("english") ||
                   teacherName.contains("lit") || teacherEmail.contains("humanities");
        }
        
        return true; // Default: any teacher can teach general subjects
    }

    private Room selectBestRoom(List<Room> rooms, Course course) {
        String subject = course.getSubject().toLowerCase();
        
        return rooms.stream()
            .filter(room -> room.getCapacity() >= 15) // Minimum capacity
            .max((r1, r2) -> {
                double score1 = calculateAdvancedRoomScore(subject, r1);
                double score2 = calculateAdvancedRoomScore(subject, r2);
                return Double.compare(score1, score2);
            })
            .orElse(null);
    }

    private double calculateAdvancedRoomScore(String subject, Room room) {
        String location = room.getLocation().toLowerCase();
        String roomName = room.getName().toLowerCase();
        double score = 0;
        
        // 🧪 CHEMISTRY COURSES - EXACT MATCHING
        if (subject.contains("chemistry") || subject.contains("organic") || subject.contains("inorganic") || 
            subject.contains("analytical") || subject.contains("biochemistry") || subject.contains("physical chemistry")) {
            if (location.contains("chemistry lab") || roomName.equals("e01")) {
                return 1000; // PERFECT MATCH: Chemistry → Chemistry Lab E01
            }
            if (location.contains("science lab") || location.contains("research lab")) {
                return 800; // GOOD: Chemistry → Science Labs
            }
            if (location.contains("lecture hall") && !location.contains("computer")) {
                return 400; // ACCEPTABLE: Chemistry → General lecture halls
            }
            return 50; // Poor match
        }
        
        // 🔬 PHYSICS COURSES - EXACT MATCHING  
        if (subject.contains("physics") || subject.contains("quantum") || subject.contains("electromagnetism") || 
            subject.contains("thermodynamics") || subject.contains("mechanics") || subject.contains("optics")) {
            if (location.contains("physics lab") || roomName.equals("e02")) {
                return 1000; // PERFECT: Physics → Physics Lab E02
            }
            if (location.contains("science lab") || location.contains("research lab")) {
                return 800; // GOOD: Physics → Science Labs
            }
            return 300; // Acceptable in general rooms
        }
        
        // 🧬 BIOLOGY COURSES - EXACT MATCHING
        if (subject.contains("biology") || subject.contains("ecology") || subject.contains("anatomy")) {
            if (location.contains("biology lab") || roomName.equals("e03")) {
                return 1000; // PERFECT: Biology → Biology Lab E03
            }
            if (location.contains("science lab") || location.contains("research lab")) {
                return 800; // GOOD: Biology → Science Labs
            }
            return 300; // Acceptable in general rooms
        }
        
        // 💻 COMPUTER SCIENCE COURSES - EXACT MATCHING
        if (subject.contains("computer") || subject.contains("programming") || subject.contains("software") || 
            subject.contains("database") || subject.contains("machine learning") || subject.contains("data structures")) {
            if (location.contains("computer lab") || roomName.startsWith("b0") && location.contains("lab")) {
                return 1000; // PERFECT: CS → Computer Labs B01-B05
            }
            if (location.contains("programming lab") || location.contains("software engineering lab")) {
                return 950; // EXCELLENT: CS → Specific CS Labs
            }
            if (location.contains("cs lecture hall") || roomName.equals("b06")) {
                return 700; // GOOD: CS → CS Lecture Hall B06
            }
            return 200; // Poor match for CS in general rooms
        }
        
        // 📊 MATHEMATICS COURSES - EXACT MATCHING
        if (subject.contains("math") || subject.contains("calculus") || subject.contains("algebra") || 
            subject.contains("statistics") || subject.contains("discrete") || subject.contains("linear algebra")) {
            if (location.contains("math") || roomName.startsWith("c0")) {
                return 1000; // PERFECT: Math → Math Classrooms C01-C06
            }
            if (location.contains("statistics lab") || roomName.equals("c03")) {
                return 950; // EXCELLENT: Statistics → Stats Lab C03
            }
            return 600; // Math works well in general lecture halls
        }
        
        // ⚙️ ENGINEERING COURSES - EXACT MATCHING
        if (subject.contains("engineering") || subject.contains("mechanical") || subject.contains("electrical") || 
            subject.contains("structural") || subject.contains("materials") || subject.contains("control systems")) {
            if (location.contains("engineering lab") || roomName.equals("d03")) {
                return 1000; // PERFECT: Engineering → Engineering Lab D03
            }
            if (location.contains("workshop") || roomName.equals("d07")) {
                return 950; // EXCELLENT: Engineering → Workshop D07
            }
            if (location.contains("cad lab") || roomName.equals("d10")) {
                return 900; // EXCELLENT: Engineering → CAD Lab D10
            }
            if (location.contains("design studio") || roomName.equals("d09")) {
                return 850; // VERY GOOD: Engineering → Design Studio D09
            }
            if (location.contains("engineering lecture hall") || roomName.equals("d08")) {
                return 700; // GOOD: Engineering → Engineering Lecture D08
            }
            return 400; // Acceptable in general rooms
        }
        
        // � BUSINESS COURSES - EXACT MATCHING
        if (subject.contains("business") || subject.contains("management") || subject.contains("economics") || 
            subject.contains("finance") || subject.contains("marketing") || subject.contains("strategic")) {
            if (location.contains("business") || roomName.startsWith("f0")) {
                return 1000; // PERFECT: Business → Business Classrooms F01-F07
            }
            if (location.contains("case study") || roomName.equals("f03")) {
                return 950; // EXCELLENT: Business → Case Study Room F03
            }
            if (location.contains("presentation") || roomName.equals("f04")) {
                return 900; // EXCELLENT: Business → Presentation Room F04
            }
            if (location.contains("conference") || roomName.equals("a03")) {
                return 800; // VERY GOOD: Business → Conference Room A03
            }
            return 500; // Good in general lecture halls
        }
        
        // 🎨 ART & DESIGN COURSES - EXACT MATCHING
        if (subject.contains("art") || subject.contains("design") || subject.contains("photography") || 
            subject.contains("digital media") || subject.contains("graphic")) {
            if (location.contains("design studio") || roomName.equals("d09")) {
                return 1000; // PERFECT: Art → Design Studio D09
            }
            return 300; // Art needs special rooms
        }
        
        // 🏥 MEDICINE & HEALTH COURSES - EXACT MATCHING
        if (subject.contains("medicine") || subject.contains("health") || subject.contains("anatomy") || 
            subject.contains("pharmacology") || subject.contains("epidemiology")) {
            if (location.contains("research lab") || roomName.equals("e05")) {
                return 1000; // PERFECT: Medicine → Research Lab E05
            }
            return 400; // Medicine works in science areas
        }
        
        // � ENVIRONMENTAL SCIENCE - EXACT MATCHING
        if (subject.contains("environment") || subject.contains("climate") || subject.contains("renewable") || 
            subject.contains("ecology")) {
            if (location.contains("science") || location.contains("research")) {
                return 800; // GOOD: Environmental → Science Labs
            }
            return 500; // Environmental works in general rooms
        }
        
        // 📚 LANGUAGE & LITERATURE - GENERAL COMPATIBILITY
        if (subject.contains("language") || subject.contains("literature") || subject.contains("english") || 
            subject.contains("spanish") || subject.contains("creative writing")) {
            return 600; // Languages work well in most rooms
        }
        
        // 🧠 PSYCHOLOGY & SOCIAL SCIENCES
        if (subject.contains("psychology") || subject.contains("sociology") || subject.contains("anthropology")) {
            return 550; // Psychology works well in general rooms
        }
        
        // 🏛️ GENERAL ROOM COMPATIBILITY
        if (location.contains("lecture hall") || location.contains("auditorium")) {
            return 400; // Good for large lectures
        }
        if (location.contains("classroom")) {
            return 350; // Good for regular classes
        }
        if (location.contains("seminar") || location.contains("tutorial")) {
            return 300; // Good for small classes
        }
        
        // 📊 CAPACITY OPTIMIZATION (Professional Standards)
        int capacity = room.getCapacity();
        if (capacity >= 25 && capacity <= 40) score += 50;      // Ideal size
        else if (capacity >= 20 && capacity <= 50) score += 30; // Good size
        else if (capacity >= 15 && capacity <= 60) score += 20; // Acceptable
        else if (capacity > 100) score -= 20;                   // Too large (wasteful)
        
        return Math.max(score, 10); // Ensure minimum score
    }

    private Event createEvent(Course course, User teacher, Room room, TimeSlot slot) {
        Event event = new Event();
        event.setTitle(course.getName());
        event.setDescription("Fast Professional Scheduling: " + course.getName());
        event.setDate(slot.getDate());
        event.setStartTime(slot.getTime());
        event.setEndTime(slot.getTime().plusMinutes(SESSION_DURATION_MINUTES));
        event.setTeacher(teacher);
        event.setRoom(room);
        event.setCourse(course);
        event.setType(EventType.COURSE);
        event.setStatus(EventStatus.SCHEDULED);
        return event;
    }

    private EventResponse createEventResponse(Event event) {
        return EventResponse.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .startTime(event.getStartTime())
            .endTime(event.getEndTime())
            .teacher(UserResponse.builder()
                .id(event.getTeacher().getId())
                .name(event.getTeacher().getName())
                .email(event.getTeacher().getEmail())
                .role(event.getTeacher().getRole())
                .build())
            .room(RoomResponse.builder()
                .id(event.getRoom().getId())
                .name(event.getRoom().getName())
                .capacity(event.getRoom().getCapacity())
                .location(event.getRoom().getLocation())
                .build())
            .build();
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
            log.info("🗑️ Cleared {} existing events", existingEvents.size());
        }
    }

    private ProfessionalScheduleResult createFailResult(String message) {
        return ProfessionalScheduleResult.builder()
            .message(message)
            .success(false)
            .build();
    }

    // Supporting classes
    private static class TimeSlot {
        private final DayOfWeek day;
        private final LocalTime time;
        private final LocalDate date;

        public TimeSlot(DayOfWeek day, LocalTime time, LocalDate date) {
            this.day = day;
            this.time = time;
            this.date = date;
        }

        public DayOfWeek getDay() { return day; }
        public LocalTime getTime() { return time; }
        public LocalDate getDate() { return date; }
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
