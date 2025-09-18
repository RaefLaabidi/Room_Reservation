package com.reservation.service;

import com.reservation.dto.request.*;
import com.reservation.dto.response.*;
import com.reservation.model.entity.Event;
import com.reservation.model.entity.Room;
import com.reservation.model.entity.User;
import com.reservation.model.entity.Availability;
import com.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final AvailabilityRepository availabilityRepository;
    private final ConflictRepository conflictRepository;

    public EventResponse createEvent(EventCreateRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Event event = Event.builder()
                .type(request.getType())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(room)
                .teacher(teacher)
                .status(request.getStatus())
                .title(request.getTitle())
                .description(request.getDescription())
                .expectedParticipants(request.getExpectedParticipants())
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<EventResponse> getEventById(Long id) {
        return eventRepository.findById(id)
                .map(this::mapToResponse);
    }

    public ScheduleGenerationResponse generateSchedule(ScheduleGenerationRequest request) {
        List<EventResponse> scheduledEvents = new ArrayList<>();
        List<UnscheduledEventResponse> unscheduledEvents = new ArrayList<>();

        for (UnscheduledEventRequest eventRequest : request.getEvents()) {
            Optional<EventResponse> scheduledEvent = scheduleEvent(eventRequest);
            
            if (scheduledEvent.isPresent()) {
                scheduledEvents.add(scheduledEvent.get());
            } else {
                unscheduledEvents.add(mapToUnscheduledResponse(eventRequest, "No available slot found"));
            }
        }

        String message = String.format("Scheduled %d out of %d events", 
                scheduledEvents.size(), request.getEvents().size());

        return ScheduleGenerationResponse.builder()
                .scheduledEvents(scheduledEvents)
                .unscheduledEvents(unscheduledEvents)
                .message(message)
                .build();
    }

    private Optional<EventResponse> scheduleEvent(UnscheduledEventRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        // Get preferred dates or use next 30 days if none provided
        List<LocalDate> datesToCheck = request.getPreferredDates() != null && !request.getPreferredDates().isEmpty()
                ? request.getPreferredDates()
                : generateDefaultDates();

        for (LocalDate date : datesToCheck) {
            // Check teacher availability
            List<Availability> teacherAvailabilities = availabilityRepository.findByTeacher(teacher)
                    .stream()
                    .filter(av -> av.getAvailableDate().equals(date))
                    .collect(Collectors.toList());

            for (Availability availability : teacherAvailabilities) {
                LocalTime startTime = request.getPreferredStartTime() != null 
                        ? request.getPreferredStartTime() 
                        : availability.getStartTime();
                
                LocalTime endTime = request.getPreferredEndTime() != null 
                        ? request.getPreferredEndTime() 
                        : availability.getEndTime();

                // Check if the requested time fits within availability
                if (startTime.isBefore(availability.getStartTime()) || 
                    endTime.isAfter(availability.getEndTime())) {
                    continue;
                }

                // Check teacher conflicts
                if (hasTeacherConflict(teacher, date, startTime, endTime)) {
                    continue;
                }

                // Find available room
                Optional<Room> availableRoom = findAvailableRoom(date, startTime, endTime, 
                        request.getExpectedParticipants());

                if (availableRoom.isPresent()) {
                    Event event = Event.builder()
                            .type(request.getType())
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .room(availableRoom.get())
                            .teacher(teacher)
                            .status(request.getStatus())
                            .title(request.getTitle())
                            .description(request.getDescription())
                            .expectedParticipants(request.getExpectedParticipants())
                            .preferredDates(request.getPreferredDates())
                            .build();

                    Event savedEvent = eventRepository.save(event);
                    return Optional.of(mapToResponse(savedEvent));
                }
            }
        }

        return Optional.empty();
    }

    private boolean hasTeacherConflict(User teacher, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Event> teacherEvents = eventRepository.findByTeacher(teacher)
                .stream()
                .filter(event -> event.getDate().equals(date))
                .filter(event -> timesOverlap(startTime, endTime, event.getStartTime(), event.getEndTime()))
                .collect(Collectors.toList());

        return !teacherEvents.isEmpty();
    }

    private Optional<Room> findAvailableRoom(LocalDate date, LocalTime startTime, LocalTime endTime, Integer expectedParticipants) {
        List<Room> allRooms = roomRepository.findAll();

        for (Room room : allRooms) {
            // Check capacity if specified
            if (expectedParticipants != null && room.getCapacity() < expectedParticipants) {
                continue;
            }

            // Check room availability
            List<Event> conflictingEvents = eventRepository.findConflictingEvents(room, date, startTime, endTime);
            if (conflictingEvents.isEmpty()) {
                return Optional.of(room);
            }
        }

        return Optional.empty();
    }

    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    private List<LocalDate> generateDefaultDates() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 30; i++) {
            dates.add(today.plusDays(i));
        }
        return dates;
    }

    public EventResponse rescheduleEvent(Long eventId, RescheduleEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check for conflicts before rescheduling
        if (hasTeacherConflict(event.getTeacher(), request.getDate(), request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("Teacher has a conflict at the requested time");
        }

        List<Event> roomConflicts = eventRepository.findConflictingEventsExcluding(
                event.getRoom(), request.getDate(), request.getStartTime(), request.getEndTime(), eventId);

        if (!roomConflicts.isEmpty()) {
            throw new RuntimeException("Room is not available at the requested time");
        }

        event.setDate(request.getDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    public EventResponse changeEventRoom(Long eventId, ChangeRoomRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Room newRoom = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check room availability
        List<Event> conflictingEvents = eventRepository.findConflictingEvents(
                newRoom, event.getDate(), event.getStartTime(), event.getEndTime());

        if (!conflictingEvents.isEmpty()) {
            throw new RuntimeException("New room is not available at the event time");
        }

        // Check capacity if event has expected participants
        if (event.getExpectedParticipants() != null && newRoom.getCapacity() < event.getExpectedParticipants()) {
            throw new RuntimeException("New room capacity is insufficient");
        }

        event.setRoom(newRoom);
        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    private EventResponse mapToResponse(Event event) {
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

    private UnscheduledEventResponse mapToUnscheduledResponse(UnscheduledEventRequest request, String reason) {
        // Create teacher response directly from repository to avoid circular dependency
        UserResponse teacher = null;
        Optional<User> teacherOpt = userRepository.findById(request.getTeacherId());
        if (teacherOpt.isPresent()) {
            User teacherEntity = teacherOpt.get();
            teacher = UserResponse.builder()
                    .id(teacherEntity.getId())
                    .name(teacherEntity.getName())
                    .email(teacherEntity.getEmail())
                    .role(teacherEntity.getRole())
                    .build();
        }
        
        return UnscheduledEventResponse.builder()
                .type(request.getType())
                .teacher(teacher)
                .title(request.getTitle())
                .description(request.getDescription())
                .expectedParticipants(request.getExpectedParticipants())
                .preferredDates(request.getPreferredDates())
                .preferredStartTime(request.getPreferredStartTime())
                .preferredEndTime(request.getPreferredEndTime())
                .reason(reason)
                .build();
    }

    public EventResponse updateEvent(Long eventId, EventUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Update fields only if they are provided in the request
        if (request.getType() != null) {
            event.setType(request.getType());
        }
        
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        
        if (request.getDate() != null) {
            event.setDate(request.getDate());
        }
        
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        
        if (request.getStatus() != null) {
            event.setStatus(request.getStatus());
        }
        
        if (request.getExpectedParticipants() != null) {
            event.setExpectedParticipants(request.getExpectedParticipants());
        }
        
        // Update teacher if provided
        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new RuntimeException("Teacher not found"));
            event.setTeacher(teacher);
        }
        
        // Update room if provided
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            event.setRoom(room);
        }
        
        // If date/time or room/teacher changed, check for conflicts
        if (request.getDate() != null || request.getStartTime() != null || 
            request.getEndTime() != null || request.getTeacherId() != null || 
            request.getRoomId() != null) {
            
            // Check teacher conflicts (excluding current event)
            if (hasTeacherConflictExcluding(event.getTeacher(), event.getDate(), 
                    event.getStartTime(), event.getEndTime(), eventId)) {
                throw new RuntimeException("Teacher has a conflict at the requested time");
            }
            
            // Check room conflicts (excluding current event)
            List<Event> roomConflicts = eventRepository.findConflictingEventsExcluding(
                    event.getRoom(), event.getDate(), event.getStartTime(), event.getEndTime(), eventId);
            
            if (!roomConflicts.isEmpty()) {
                throw new RuntimeException("Room is not available at the requested time");
            }
            
            // Check room capacity if participants specified
            if (event.getExpectedParticipants() != null && 
                event.getRoom().getCapacity() < event.getExpectedParticipants()) {
                throw new RuntimeException("Room capacity is insufficient for expected participants");
            }
        }

        Event savedEvent = eventRepository.save(event);
        return mapToResponse(savedEvent);
    }

    private boolean hasTeacherConflictExcluding(User teacher, LocalDate date, 
            LocalTime startTime, LocalTime endTime, Long excludeEventId) {
        List<Event> teacherEvents = eventRepository.findByTeacher(teacher)
                .stream()
                .filter(event -> !event.getId().equals(excludeEventId)) // Exclude current event
                .filter(event -> event.getDate().equals(date))
                .filter(event -> timesOverlap(startTime, endTime, event.getStartTime(), event.getEndTime()))
                .collect(Collectors.toList());

        return !teacherEvents.isEmpty();
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        System.out.println("Attempting to delete event with ID: " + eventId);
        
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        
        Event event = eventRepository.findById(eventId).get();
        System.out.println("Found event: " + event.getId() + " - " + event.getType());
        
        try {
            // First, delete all conflicts involving this event
            var conflicts = conflictRepository.findByEvent(event);
            if (!conflicts.isEmpty()) {
                System.out.println("Deleting " + conflicts.size() + " conflicts involving this event");
                conflictRepository.deleteAll(conflicts);
            }
            
            // Clear the preferred dates collection to avoid constraint issues
            if (event.getPreferredDates() != null && !event.getPreferredDates().isEmpty()) {
                System.out.println("Clearing preferred dates collection: " + event.getPreferredDates().size() + " items");
                event.getPreferredDates().clear();
                eventRepository.saveAndFlush(event);
            }
            
            // Now delete the event using deleteById
            System.out.println("Deleting event...");
            eventRepository.deleteById(eventId);
            System.out.println("Event deleted successfully");
            
            // Verify deletion
            if (eventRepository.existsById(eventId)) {
                throw new RuntimeException("Event still exists after deletion attempt");
            }
        } catch (Exception e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete event: " + e.getMessage(), e);
        }
    }
}
