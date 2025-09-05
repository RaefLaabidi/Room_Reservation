package com.reservation.controller;

import com.reservation.model.entity.Event;
import com.reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events-enhanced")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class EnhancedEventController {

    private final EventRepository eventRepository;

    @GetMapping
    public ResponseEntity<List<EnhancedEventDto>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        List<EnhancedEventDto> eventDtos = events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventDtos);
    }

    @GetMapping("/week/{date}")
    public ResponseEntity<List<EnhancedEventDto>> getEventsForWeek(@PathVariable String date) {
        LocalDate startDate = LocalDate.parse(date);
        LocalDate endDate = startDate.plusDays(6);
        
        List<Event> events = eventRepository.findByDateBetween(startDate, endDate);
        List<EnhancedEventDto> eventDtos = events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventDtos);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnhancedEventDto>> getEventsByCourse(@PathVariable Long courseId) {
        // Temporarily disabled until migration is complete
        return ResponseEntity.ok(List.of());
        /*
        List<Event> events = eventRepository.findByCourseId(courseId);
        List<EnhancedEventDto> eventDtos = events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(eventDtos);
        */
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEventStatistics() {
        List<Event> allEvents = eventRepository.findAll();
        
        Map<String, Object> stats = Map.of(
            "totalEvents", allEvents.size(),
            "courseEvents", allEvents.stream().filter(e -> e.getCourse() != null).count(),
            "nonCourseEvents", allEvents.stream().filter(e -> e.getCourse() == null).count(),
            "eventsByType", allEvents.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getType().name(),
                    Collectors.counting()
                )),
            "eventsByStatus", allEvents.stream()
                .collect(Collectors.groupingBy(
                    e -> e.getStatus().name(),
                    Collectors.counting()
                )),
            "courseEventsBySubject", allEvents.stream()
                .filter(e -> e.getCourse() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getCourse().getSubject(),
                    Collectors.counting()
                ))
        );
        
        return ResponseEntity.ok(stats);
    }

    private EnhancedEventDto convertToDto(Event event) {
        return EnhancedEventDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .type(event.getType().name())
                .status(event.getStatus().name())
                .expectedParticipants(event.getExpectedParticipants())
                .teacher(event.getTeacher() != null ? TeacherDto.builder()
                        .id(event.getTeacher().getId())
                        .name(event.getTeacher().getName())
                        .email(event.getTeacher().getEmail())
                        .build() : null)
                .room(event.getRoom() != null ? RoomDto.builder()
                        .id(event.getRoom().getId())
                        .name(event.getRoom().getName())
                        .location(event.getRoom().getLocation())
                        .capacity(event.getRoom().getCapacity())
                        .build() : null)
                .course(event.getCourse() != null ? CourseDto.builder()
                        .id(event.getCourse().getId())
                        .name(event.getCourse().getName())
                        .subject(event.getCourse().getSubject())
                        .durationHours(event.getCourse().getDurationHours())
                        .sessionsPerWeek(event.getCourse().getSessionsPerWeek())
                        .minCapacity(event.getCourse().getMinCapacity())
                        .department(event.getCourse().getDepartment())
                        .build() : null)
                .build();
    }

    // DTO Classes
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EnhancedEventDto {
        private Long id;
        private String title;
        private String description;
        private java.time.LocalDate date;
        private java.time.LocalTime startTime;
        private java.time.LocalTime endTime;
        private String type;
        private String status;
        private Integer expectedParticipants;
        private TeacherDto teacher;
        private RoomDto room;
        private CourseDto course;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TeacherDto {
        private Long id;
        private String name;
        private String email;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RoomDto {
        private Long id;
        private String name;
        private String location;
        private Integer capacity;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CourseDto {
        private Long id;
        private String name;
        private String subject;
        private Integer durationHours;
        private Integer sessionsPerWeek;
        private Integer minCapacity;
        private String department;
    }
}
