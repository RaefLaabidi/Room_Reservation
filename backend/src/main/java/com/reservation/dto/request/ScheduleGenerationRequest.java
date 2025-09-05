package com.reservation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleGenerationRequest {
    
    @NotEmpty(message = "Events list cannot be empty")
    @Valid
    private List<UnscheduledEventRequest> events;
}
