package com.reservation.service;

import com.reservation.model.entity.Event;
import com.reservation.model.entity.Room;
import com.reservation.model.entity.Course;
import com.reservation.repository.EventRepository;
import com.reservation.repository.RoomRepository;
import com.reservation.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulingAnalyticsService {
    
    private final EventRepository eventRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    
    @Transactional(readOnly = true)
    public Map<String, Object> getComprehensiveAnalytics(String timeRange) {
        log.info("📊 Generating comprehensive scheduling analytics");
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            // Use JOIN FETCH to ensure room and course relationships are loaded
            List<Event> events = eventRepository.findAllForAnalytics();
            List<Room> rooms = roomRepository.findAll();
            List<Course> courses = courseRepository.findAll();
            
            log.info("Found {} events, {} rooms, {} courses for analysis", events.size(), rooms.size(), courses.size());
            
            // Debug: Log first few events with detailed info
            if (!events.isEmpty()) {
                Event firstEvent = events.get(0);
                log.info("Sample event - ID: {}, Title: '{}', Room: {}, Course: {}, Date: {}, StartTime: {}", 
                    firstEvent.getId(), 
                    firstEvent.getTitle(), 
                    firstEvent.getRoom() != null ? firstEvent.getRoom().getName() : "NULL",
                    firstEvent.getCourse() != null ? firstEvent.getCourse().getName() : "NULL",
                    firstEvent.getDate(),
                    firstEvent.getStartTime());
                
                // Log room relationship details
                if (firstEvent.getRoom() != null) {
                    log.info("First event room details - Room ID: {}, Name: '{}'", 
                        firstEvent.getRoom().getId(), firstEvent.getRoom().getName());
                } else {
                    log.warn("First event has NULL room relationship!");
                }
            }

            // Count events with/without rooms
            long eventsWithRooms = events.stream().filter(e -> e.getRoom() != null).count();
            long eventsWithoutRooms = events.size() - eventsWithRooms;
            log.info("Events with rooms: {}, Events without rooms: {}", eventsWithRooms, eventsWithoutRooms);

            // Basic counts
            analytics.put("totalScheduledEvents", events.size());
            
            // Count unique courses using both title and course entity
            Set<String> uniqueCourses = new HashSet<>();
            for (Event e : events) {
                if (e.getTitle() != null) {
                    uniqueCourses.add(e.getTitle());
                }
                if (e.getCourse() != null && e.getCourse().getName() != null) {
                    uniqueCourses.add(e.getCourse().getName());
                }
            }
            analytics.put("totalUniqueCourses", uniqueCourses.size());
            
            // Count unique rooms used
            Set<String> uniqueRooms = new HashSet<>();
            for (Event e : events) {
                if (e.getRoom() != null && e.getRoom().getName() != null) {
                    uniqueRooms.add(e.getRoom().getName());
                }
            }
            analytics.put("totalRoomsUsed", uniqueRooms.size());

            // Room utilization - safe approach
            Map<String, Long> roomUtilization = new HashMap<>();
            for (Event e : events) {
                if (e.getRoom() != null && e.getRoom().getName() != null) {
                    String roomName = e.getRoom().getName();
                    roomUtilization.put(roomName, roomUtilization.getOrDefault(roomName, 0L) + 1);
                }
            }
            analytics.put("roomUtilization", roomUtilization);
            
            log.info("Room utilization map: {}", roomUtilization);

            // Time distribution
            Map<String, Long> timeDistribution = events.stream()
                .filter(e -> e.getStartTime() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    Collectors.counting()
                ));
            analytics.put("timeDistribution", timeDistribution);

            // Day distribution
            Map<String, Long> dayDistribution = events.stream()
                .filter(e -> e.getDate() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getDate().getDayOfWeek().toString(),
                    Collectors.counting()
                ));
            analytics.put("dayDistribution", dayDistribution);

            // Course distribution - safe approach using both title and course entity
            Map<String, Long> courseDistribution = new HashMap<>();
            for (Event e : events) {
                String courseName = null;
                if (e.getCourse() != null && e.getCourse().getName() != null) {
                    courseName = e.getCourse().getName();
                } else if (e.getTitle() != null) {
                    courseName = e.getTitle();
                }
                if (courseName != null) {
                    courseDistribution.put(courseName, courseDistribution.getOrDefault(courseName, 0L) + 1);
                }
            }
            analytics.put("courseDistribution", courseDistribution);

            log.info("✅ Successfully generated comprehensive analytics with {} room utilizations, {} course distributions", 
                roomUtilization.size(), courseDistribution.size());
            
        } catch (Exception e) {
            log.error("❌ Error generating comprehensive analytics: {}", e.getMessage(), e);
            analytics = getEmptyComprehensiveAnalytics();
        }

        return analytics;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRoomUtilizationStats() {
        log.info("📊 Generating room utilization stats");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Use JOIN FETCH to ensure room and course relationships are loaded
            List<Event> events = eventRepository.findAllForAnalytics();
            List<Room> rooms = roomRepository.findAll();
            
            log.info("📊 Room Analysis - Processing {} events and {} rooms", events.size(), rooms.size());

            // Debug: Check room relationships in detail
            int eventsWithRooms = 0;
            int eventsWithoutRooms = 0;
            Map<String, Integer> roomDebugCount = new HashMap<>();
            
            for (Event e : events) {
                if (e.getRoom() != null) {
                    eventsWithRooms++;
                    String roomName = e.getRoom().getName();
                    roomDebugCount.put(roomName, roomDebugCount.getOrDefault(roomName, 0) + 1);
                    log.info("Event {} has room: ID={}, Name='{}' (count so far: {})", 
                        e.getId(), e.getRoom().getId(), roomName, roomDebugCount.get(roomName));
                } else {
                    eventsWithoutRooms++;
                    log.warn("Event {} (title: '{}') has NO room relationship!", e.getId(), e.getTitle());
                }
            }
            log.info("📊 ROOM DEBUG SUMMARY - Events with rooms: {}, Events without rooms: {}", eventsWithRooms, eventsWithoutRooms);
            log.info("📊 ROOM DEBUG COUNTS: {}", roomDebugCount);

            // Room usage count - safe approach
            Map<String, Long> roomUsage = new HashMap<>();
            for (Event e : events) {
                if (e.getRoom() != null && e.getRoom().getName() != null) {
                    String roomName = e.getRoom().getName();
                    roomUsage.put(roomName, roomUsage.getOrDefault(roomName, 0L) + 1);
                    log.debug("Added usage for room: {}", roomName);
                }
            }
            log.info("Room usage map: {}", roomUsage);

            // Room capacity
            Map<String, Integer> roomCapacity = rooms.stream()
                .collect(Collectors.toMap(
                    Room::getName,
                    Room::getCapacity
                ));

            // Calculate utilization percentage
            Map<String, Double> utilizationPercentage = new HashMap<>();
            long maxUsage = roomUsage.values().stream().mapToLong(Long::longValue).max().orElse(1);
            
            for (Room room : rooms) {
                Long usage = roomUsage.getOrDefault(room.getName(), 0L);
                double percentage = maxUsage > 0 ? (usage.doubleValue() / maxUsage) * 100.0 : 0.0;
                utilizationPercentage.put(room.getName(), Math.round(percentage * 100.0) / 100.0);
            }

            stats.put("roomUsage", roomUsage);
            stats.put("roomCapacity", roomCapacity);
            stats.put("utilizationPercentage", utilizationPercentage);
            stats.put("totalRooms", rooms.size());
            stats.put("averageUtilization", 
                utilizationPercentage.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0));

            log.info("✅ Successfully generated room utilization stats");

        } catch (Exception e) {
            log.error("❌ Error generating room utilization stats: {}", e.getMessage(), e);
            stats = getEmptyRoomStats();
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTimeDistributionStats() {
        log.info("📊 Generating time distribution stats");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Event> events = eventRepository.findAll();

            // Hourly distribution
            Map<String, Long> hourlyDistribution = events.stream()
                .filter(e -> e.getStartTime() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    Collectors.counting()
                ));

            // Daily distribution
            Map<String, Long> dailyDistribution = events.stream()
                .filter(e -> e.getDate() != null)
                .collect(Collectors.groupingBy(
                    e -> e.getDate().getDayOfWeek().toString(),
                    Collectors.counting()
                ));

            // Find peak hour and day
            String peakHour = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

            String peakDay = dailyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

            stats.put("hourlyDistribution", hourlyDistribution);
            stats.put("dailyDistribution", dailyDistribution);
            stats.put("peakHour", peakHour);
            stats.put("peakDay", peakDay);
            stats.put("totalEvents", events.size());

            log.info("✅ Successfully generated time distribution stats");

        } catch (Exception e) {
            log.error("❌ Error generating time distribution stats: {}", e.getMessage(), e);
            stats = getEmptyTimeStats();
        }

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSubjectRoomMatchingStats() {
        log.info("📊 Generating subject-room matching stats");
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Event> events = eventRepository.findAllForAnalytics();
            log.info("Processing {} events for subject-room matching", events.size());

            // Course-room combinations - safe approach
            Map<String, Long> courseRoomCount = new HashMap<>();
            Map<String, Long> roomCourseCount = new HashMap<>();
            
            for (Event e : events) {
                String courseName = null;
                String roomName = null;
                
                // Get course name from either course entity or title
                if (e.getCourse() != null && e.getCourse().getName() != null) {
                    courseName = e.getCourse().getName();
                } else if (e.getTitle() != null) {
                    courseName = e.getTitle();
                }
                
                // Get room name
                if (e.getRoom() != null && e.getRoom().getName() != null) {
                    roomName = e.getRoom().getName();
                }
                
                if (courseName != null) {
                    courseRoomCount.put(courseName, courseRoomCount.getOrDefault(courseName, 0L) + 1);
                }
                
                if (roomName != null) {
                    roomCourseCount.put(roomName, roomCourseCount.getOrDefault(roomName, 0L) + 1);
                }
            }
            
            log.info("Course counts: {}, Room counts: {}", courseRoomCount.size(), roomCourseCount.size());

            // Find most versatile room and mobile course
            String mostVersatileRoom = "N/A";
            String mostMobileCourse = "N/A";
            
            if (!events.isEmpty()) {
                // Room to unique courses mapping - safe approach
                Map<String, Set<String>> roomToCourses = new HashMap<>();
                Map<String, Set<String>> courseToRooms = new HashMap<>();
                
                for (Event e : events) {
                    String courseName = null;
                    String roomName = null;
                    
                    if (e.getCourse() != null && e.getCourse().getName() != null) {
                        courseName = e.getCourse().getName();
                    } else if (e.getTitle() != null) {
                        courseName = e.getTitle();
                    }
                    
                    if (e.getRoom() != null && e.getRoom().getName() != null) {
                        roomName = e.getRoom().getName();
                    }
                    
                    if (courseName != null && roomName != null) {
                        roomToCourses.computeIfAbsent(roomName, k -> new HashSet<>()).add(courseName);
                        courseToRooms.computeIfAbsent(courseName, k -> new HashSet<>()).add(roomName);
                    }
                }

                mostVersatileRoom = roomToCourses.entrySet().stream()
                    .max(Map.Entry.comparingByValue(Comparator.comparing(Set::size)))
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

                mostMobileCourse = courseToRooms.entrySet().stream()
                    .max(Map.Entry.comparingByValue(Comparator.comparing(Set::size)))
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
            }

            // Count unique combinations - safe approach
            Set<String> uniqueCombinations = new HashSet<>();
            for (Event e : events) {
                String courseName = null;
                String roomName = null;
                
                if (e.getCourse() != null && e.getCourse().getName() != null) {
                    courseName = e.getCourse().getName();
                } else if (e.getTitle() != null) {
                    courseName = e.getTitle();
                }
                
                if (e.getRoom() != null && e.getRoom().getName() != null) {
                    roomName = e.getRoom().getName();
                }
                
                if (courseName != null && roomName != null) {
                    uniqueCombinations.add(courseName + "-" + roomName);
                }
            }

            stats.put("courseRoomCount", courseRoomCount);
            stats.put("roomCourseCount", roomCourseCount);
            stats.put("mostVersatileRoom", mostVersatileRoom);
            stats.put("mostMobileCourse", mostMobileCourse);
            stats.put("totalUniqueCombinations", uniqueCombinations.size());

            log.info("✅ Successfully generated subject-room matching stats with {} unique combinations", uniqueCombinations.size());

        } catch (Exception e) {
            log.error("❌ Error generating subject-room matching stats: {}", e.getMessage(), e);
            stats = getEmptyMatchingStats();
        }

        return stats;
    }

    // Helper methods for empty responses
    private Map<String, Object> getEmptyComprehensiveAnalytics() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("totalScheduledEvents", 0);
        empty.put("totalUniqueCourses", 0);
        empty.put("totalRoomsUsed", 0);
        empty.put("roomUtilization", new HashMap<>());
        empty.put("timeDistribution", new HashMap<>());
        empty.put("dayDistribution", new HashMap<>());
        empty.put("courseDistribution", new HashMap<>());
        return empty;
    }

    private Map<String, Object> getEmptyRoomStats() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("roomUsage", new HashMap<>());
        empty.put("roomCapacity", new HashMap<>());
        empty.put("utilizationPercentage", new HashMap<>());
        empty.put("totalRooms", 0);
        empty.put("averageUtilization", 0.0);
        return empty;
    }

    private Map<String, Object> getEmptyTimeStats() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("hourlyDistribution", new HashMap<>());
        empty.put("dailyDistribution", new HashMap<>());
        empty.put("peakHour", "N/A");
        empty.put("peakDay", "N/A");
        empty.put("totalEvents", 0);
        return empty;
    }

    private Map<String, Object> getEmptyMatchingStats() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("courseRoomCount", new HashMap<>());
        empty.put("roomCourseCount", new HashMap<>());
        empty.put("mostVersatileRoom", "N/A");
        empty.put("mostMobileCourse", "N/A");
        empty.put("totalUniqueCombinations", 0);
        return empty;
    }
}
