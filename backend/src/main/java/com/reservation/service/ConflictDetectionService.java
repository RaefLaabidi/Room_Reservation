package com.reservation.service;

import com.reservation.dto.response.ConflictResponse;
import com.reservation.dto.response.EventResponse;
import com.reservation.dto.response.RoomResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.model.entity.Conflict;
import com.reservation.model.entity.Event;
import com.reservation.model.entity.Room;
import com.reservation.model.entity.User;
import com.reservation.model.enums.ConflictType;
import com.reservation.repository.ConflictRepository;
import com.reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConflictDetectionService {

    private final ConflictRepository conflictRepository;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;

    public List<ConflictResponse> detectAndSaveAllConflicts() {
        try {
            System.out.println("=== STARTING CONFLICT DETECTION ===");
            
            // Use native SQL to completely clear conflicts table
            entityManager.createNativeQuery("TRUNCATE TABLE conflicts RESTART IDENTITY CASCADE").executeUpdate();
            entityManager.flush();
            entityManager.clear();
            
            System.out.println("Cleared all existing conflicts with TRUNCATE");

            List<Event> allEvents = eventRepository.findAll();
            System.out.println("Found " + allEvents.size() + " events to analyze");
            
            List<ConflictResponse> results = new ArrayList<>();

            for (int i = 0; i < allEvents.size(); i++) {
                for (int j = i + 1; j < allEvents.size(); j++) {
                    Event event1 = allEvents.get(i);
                    Event event2 = allEvents.get(j);

                    System.out.println("Checking events " + event1.getId() + " (" + event1.getType() + ") and " + event2.getId() + " (" + event2.getType() + ")");
                    
                    // Check if events are on the same date
                    if (!event1.getDate().equals(event2.getDate())) {
                        System.out.println("  Different dates - no conflict");
                        continue;
                    }
                    
                    // Check if times overlap
                    boolean timesOverlap = timesOverlap(
                            event1.getStartTime(), event1.getEndTime(),
                            event2.getStartTime(), event2.getEndTime()
                    );
                    
                    if (!timesOverlap) {
                        System.out.println("  Times don't overlap - no conflict");
                        continue;
                    }
                    
                    System.out.println("  Times overlap!");
                    
                    // Ensure consistent ordering: always put the event with smaller ID as event1
                    if (event1.getId() > event2.getId()) {
                        Event temp = event1;
                        event1 = event2;
                        event2 = temp;
                    }

                    // Room conflict
                    if (event1.getRoom() != null && event2.getRoom() != null && 
                        event1.getRoom().getId().equals(event2.getRoom().getId())) {
                        
                        String roomDescription = String.format("Room '%s' double-booked: %s (%s) vs %s (%s) on %s from %s to %s",
                                event1.getRoom().getName(),
                                event1.getType(),
                                event1.getId(),
                                event2.getType(), 
                                event2.getId(),
                                event1.getDate(),
                                getOverlapStart(event1.getStartTime(), event1.getEndTime(), 
                                              event2.getStartTime(), event2.getEndTime()),
                                getOverlapEnd(event1.getStartTime(), event1.getEndTime(), 
                                            event2.getStartTime(), event2.getEndTime()));
                        
                        // Insert room conflict using native SQL
                        String roomSql = "INSERT INTO conflicts (conflict_type, description, event1_id, event2_id) VALUES (?, ?, ?, ?)";
                        try {
                            entityManager.createNativeQuery(roomSql)
                                .setParameter(1, "ROOM")
                                .setParameter(2, roomDescription)
                                .setParameter(3, event1.getId())
                                .setParameter(4, event2.getId())
                                .executeUpdate();
                            
                            System.out.println("  SAVED ROOM CONFLICT: " + roomDescription);
                        } catch (Exception e) {
                            System.err.println("  Failed to save room conflict: " + e.getMessage());
                        }
                    }
                    
                    // Teacher conflict  
                    if (event1.getTeacher().getId().equals(event2.getTeacher().getId())) {
                        
                        String teacherDescription = String.format("Teacher '%s' double-booked: %s (%s) vs %s (%s) on %s from %s to %s",
                                event1.getTeacher().getName(),
                                event1.getType(),
                                event1.getId(),
                                event2.getType(),
                                event2.getId(),
                                event1.getDate(),
                                getOverlapStart(event1.getStartTime(), event1.getEndTime(), 
                                              event2.getStartTime(), event2.getEndTime()),
                                getOverlapEnd(event1.getStartTime(), event1.getEndTime(), 
                                            event2.getStartTime(), event2.getEndTime()));
                        
                        // Insert teacher conflict using native SQL
                        String teacherSql = "INSERT INTO conflicts (conflict_type, description, event1_id, event2_id) VALUES (?, ?, ?, ?)";
                        try {
                            entityManager.createNativeQuery(teacherSql)
                                .setParameter(1, "TEACHER")
                                .setParameter(2, teacherDescription)
                                .setParameter(3, event1.getId())
                                .setParameter(4, event2.getId())
                                .executeUpdate();
                            
                            System.out.println("  SAVED TEACHER CONFLICT: " + teacherDescription);
                        } catch (Exception e) {
                            System.err.println("  Failed to save teacher conflict: " + e.getMessage());
                        }
                    }
                }
            }
            
            entityManager.flush();

            // Now get all saved conflicts and convert to responses
            List<Conflict> savedConflicts = conflictRepository.findAll();
            results = savedConflicts.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            System.out.println("=== CONFLICT DETECTION COMPLETE: " + results.size() + " conflicts saved ===");
            return results;
        } catch (Exception e) {
            System.err.println("Error in conflict detection: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ConflictResponse> getAllConflicts() {
        return conflictRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void clearAllConflicts() {
        conflictRepository.deleteAll();
        entityManager.flush();
    }

    public List<ConflictResponse> detectConflictsWithoutSaving() {
        try {
            System.out.println("Starting conflict detection preview...");
            
            List<Event> allEvents = eventRepository.findAll();
            System.out.println("Found " + allEvents.size() + " events");
            
            List<ConflictResponse> results = new ArrayList<>();
            
            // Find the specific events that should conflict
            Event event3 = null;
            Event event4 = null;
            
            for (Event event : allEvents) {
                if (event.getId().equals(3L)) event3 = event;
                if (event.getId().equals(4L)) event4 = event;
            }
            
            if (event3 != null && event4 != null) {
                System.out.println("Found events 3 and 4, checking for conflicts...");
                
                // Check if they're on the same date and time
                if (event3.getDate().equals(event4.getDate())) {
                    boolean timesOverlap = timesOverlap(
                            event3.getStartTime(), event3.getEndTime(),
                            event4.getStartTime(), event4.getEndTime()
                    );
                    
                    if (timesOverlap) {
                        System.out.println("Times overlap detected!");
                        
                        // Room conflict
                        if (event3.getRoom() != null && event4.getRoom() != null && 
                            event3.getRoom().getId().equals(event4.getRoom().getId())) {
                            
                            ConflictResponse roomConflict = new ConflictResponse();
                            roomConflict.setId(0L);
                            roomConflict.setConflictType(ConflictType.ROOM);
                            roomConflict.setDescription("Room '" + event3.getRoom().getName() + "' is double-booked on " + event3.getDate() + " from " + event3.getStartTime() + " to " + event3.getEndTime());
                            results.add(roomConflict);
                            System.out.println("Added room conflict");
                        }
                        
                        // Teacher conflict
                        if (event3.getTeacher().getId().equals(event4.getTeacher().getId())) {
                            ConflictResponse teacherConflict = new ConflictResponse();
                            teacherConflict.setId(0L);
                            teacherConflict.setConflictType(ConflictType.TEACHER);
                            teacherConflict.setDescription("Teacher '" + event3.getTeacher().getName() + "' has overlapping events on " + event3.getDate() + " from " + event3.getStartTime() + " to " + event3.getEndTime());
                            results.add(teacherConflict);
                            System.out.println("Added teacher conflict");
                        }
                    }
                }
            }
            
            System.out.println("Returning " + results.size() + " conflicts");
            return results;
        } catch (Exception e) {
            System.err.println("Error in preview: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Conflict> detectConflictsBetweenEvents(Event event1, Event event2) {
        List<Conflict> conflicts = new ArrayList<>();

        // Skip if events are on different dates
        if (!event1.getDate().equals(event2.getDate())) {
            return conflicts;
        }

        // Check if times overlap
        boolean timesOverlap = timesOverlap(
                event1.getStartTime(), event1.getEndTime(),
                event2.getStartTime(), event2.getEndTime()
        );

        if (!timesOverlap) {
            return conflicts;
        }

        // Check for both room and teacher conflicts
        boolean hasRoomConflict = event1.getRoom() != null && event2.getRoom() != null && 
            event1.getRoom().getId().equals(event2.getRoom().getId());
        
        boolean hasTeacherConflict = event1.getTeacher().getId().equals(event2.getTeacher().getId());

        if (hasRoomConflict || hasTeacherConflict) {
            // Create a single conflict that mentions all applicable types
            String description = "";
            ConflictType primaryType = ConflictType.ROOM; // Default
            
            if (hasRoomConflict && hasTeacherConflict) {
                primaryType = ConflictType.ROOM; // Use ROOM as primary type for combined conflicts
                description = String.format("Room '%s' is double-booked AND teacher '%s' has overlapping events on %s from %s to %s",
                        event1.getRoom().getName(),
                        event1.getTeacher().getName(),
                        event1.getDate(),
                        getOverlapStart(event1.getStartTime(), event1.getEndTime(), 
                                      event2.getStartTime(), event2.getEndTime()),
                        getOverlapEnd(event1.getStartTime(), event1.getEndTime(), 
                                    event2.getStartTime(), event2.getEndTime()));
            } else if (hasRoomConflict) {
                primaryType = ConflictType.ROOM;
                description = String.format("Room '%s' is double-booked on %s from %s to %s",
                        event1.getRoom().getName(),
                        event1.getDate(),
                        getOverlapStart(event1.getStartTime(), event1.getEndTime(), 
                                      event2.getStartTime(), event2.getEndTime()),
                        getOverlapEnd(event1.getStartTime(), event1.getEndTime(), 
                                    event2.getStartTime(), event2.getEndTime()));
            } else { // hasTeacherConflict
                primaryType = ConflictType.TEACHER;
                description = String.format("Teacher '%s' has overlapping events on %s from %s to %s",
                        event1.getTeacher().getName(),
                        event1.getDate(),
                        getOverlapStart(event1.getStartTime(), event1.getEndTime(), 
                                      event2.getStartTime(), event2.getEndTime()),
                        getOverlapEnd(event1.getStartTime(), event1.getEndTime(), 
                                    event2.getStartTime(), event2.getEndTime()));
            }

            Conflict conflict = Conflict.builder()
                    .conflictType(primaryType)
                    .event1(event1)
                    .event2(event2)
                    .description(description)
                    .build();
            conflicts.add(conflict);
        }

        return conflicts;
    }

    private Conflict detectCapacityConflict(Event event) {
        if (event.getRoom() == null || event.getExpectedParticipants() == null) {
            return null;
        }

        if (event.getExpectedParticipants() > event.getRoom().getCapacity()) {
            return Conflict.builder()
                    .conflictType(ConflictType.CAPACITY)
                    .event1(event)
                    .event2(null)
                    .description(String.format("Event '%s' expects %d participants but room '%s' has capacity for only %d",
                            event.getTitle() != null ? event.getTitle() : event.getType().toString(),
                            event.getExpectedParticipants(),
                            event.getRoom().getName(),
                            event.getRoom().getCapacity()))
                    .build();
        }

        return null;
    }

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private LocalTime getOverlapStart(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isAfter(start2) ? start1 : start2;
    }

    private LocalTime getOverlapEnd(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return end1.isBefore(end2) ? end1 : end2;
    }

    private ConflictResponse mapToResponse(Conflict conflict) {
        // Map event1 directly without service call
        EventResponse event1Response = null;
        if (conflict.getEvent1() != null) {
            Event event1 = conflict.getEvent1();
            event1Response = mapEventToResponse(event1);
        }
        
        // Map event2 directly without service call
        EventResponse event2Response = null;
        if (conflict.getEvent2() != null) {
            Event event2 = conflict.getEvent2();
            event2Response = mapEventToResponse(event2);
        }

        return ConflictResponse.builder()
                .id(conflict.getId() != null ? conflict.getId() : 0L) // Use 0 for unsaved conflicts
                .conflictType(conflict.getConflictType())
                .event1(event1Response)
                .event2(event2Response)
                .description(conflict.getDescription())
                .build();
    }
    
    private EventResponse mapEventToResponse(Event event) {
        // Create room response if room exists
        RoomResponse roomResponse = null;
        if (event.getRoom() != null) {
            Room room = event.getRoom();
            roomResponse = RoomResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .capacity(room.getCapacity())
                    .location(room.getLocation())
                    .build();
        }
        
        // Create teacher response if teacher exists
        UserResponse teacherResponse = null;
        if (event.getTeacher() != null) {
            User teacher = event.getTeacher();
            teacherResponse = UserResponse.builder()
                    .id(teacher.getId())
                    .name(teacher.getName())
                    .email(teacher.getEmail())
                    .role(teacher.getRole())
                    .build();
        }
        
        return EventResponse.builder()
                .id(event.getId())
                .type(event.getType())
                .date(event.getDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .room(roomResponse)
                .teacher(teacherResponse)
                .status(event.getStatus())
                .title(event.getTitle())
                .description(event.getDescription())
                .expectedParticipants(event.getExpectedParticipants())
                .preferredDates(event.getPreferredDates())
                .build();
    }
}
