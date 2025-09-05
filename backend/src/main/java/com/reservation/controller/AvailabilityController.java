package com.reservation.controller;

import com.reservation.dto.request.AvailabilityCreateRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<AvailabilityResponse> createAvailability(@Valid @RequestBody AvailabilityCreateRequest request) {
        AvailabilityResponse response = availabilityService.createAvailability(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> getAllAvailabilities() {
        List<AvailabilityResponse> availabilities = availabilityService.getAllAvailabilities();
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponse> getAvailabilityById(@PathVariable Long id) {
        return availabilityService.getAvailabilityById(id)
                .map(availability -> ResponseEntity.ok(availability))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilitiesByTeacher(@PathVariable Long teacherId) {
        List<AvailabilityResponse> availabilities = availabilityService.getAvailabilitiesByTeacher(teacherId);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilitiesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AvailabilityResponse> availabilities = availabilityService.getAvailabilitiesByDate(date);
        return ResponseEntity.ok(availabilities);
    }
}
