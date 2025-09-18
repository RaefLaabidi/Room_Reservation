package com.reservation.controller;

import com.reservation.dto.request.*;
import com.reservation.dto.response.*;
import com.reservation.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        EventResponse response = eventService.createEvent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(event -> ResponseEntity.ok(event))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id, 
            @Valid @RequestBody EventUpdateRequest request) {
        try {
            EventResponse response = eventService.updateEvent(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("Update event failed: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<ScheduleGenerationResponse> generateSchedule(
            @Valid @RequestBody ScheduleGenerationRequest request) {
        ScheduleGenerationResponse response = eventService.generateSchedule(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<EventResponse> rescheduleEvent(
            @PathVariable Long id, 
            @Valid @RequestBody RescheduleEventRequest request) {
        try {
            EventResponse response = eventService.rescheduleEvent(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/change-room")
    public ResponseEntity<EventResponse> changeEventRoom(
            @PathVariable Long id, 
            @Valid @RequestBody ChangeRoomRequest request) {
        try {
            EventResponse response = eventService.changeEventRoom(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        System.out.println("DELETE endpoint called for event ID: " + id);
        try {
            eventService.deleteEvent(id);
            System.out.println("Delete service completed successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("Delete failed with error: " + e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}
