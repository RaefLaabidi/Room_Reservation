package com.reservation.service;

import com.reservation.dto.request.AvailabilityCreateRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.model.entity.Availability;
import com.reservation.model.entity.User;
import com.reservation.repository.AvailabilityRepository;
import com.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public AvailabilityResponse createAvailability(AvailabilityCreateRequest request) {
        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Availability availability = Availability.builder()
                .teacher(teacher)
                .availableDate(request.getAvailableDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Availability savedAvailability = availabilityRepository.save(availability);
        return mapToResponse(savedAvailability);
    }

    public List<AvailabilityResponse> getAllAvailabilities() {
        return availabilityRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Optional<AvailabilityResponse> getAvailabilityById(Long id) {
        return availabilityRepository.findById(id)
                .map(this::mapToResponse);
    }

    public List<AvailabilityResponse> getAvailabilitiesByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        return availabilityRepository.findByTeacher(teacher)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AvailabilityResponse> getAvailabilitiesByDate(LocalDate date) {
        return availabilityRepository.findByAvailableDate(date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AvailabilityResponse mapToResponse(Availability availability) {
        UserResponse teacher = userService.getUserById(availability.getTeacher().getId()).orElse(null);
        
        return AvailabilityResponse.builder()
                .id(availability.getId())
                .teacher(teacher)
                .availableDate(availability.getAvailableDate())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .build();
    }
}
