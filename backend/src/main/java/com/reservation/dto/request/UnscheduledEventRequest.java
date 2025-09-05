package com.reservation.dto.request;

import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnscheduledEventRequest {
    
    @NotNull(message = "Event type is required")
    private EventType type;
    
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    private String title;
    
    private String description;
    
    private Integer expectedParticipants;
    
    private List<LocalDate> preferredDates;
    
    private LocalTime preferredStartTime;
    
    private LocalTime preferredEndTime;
    
    @Builder.Default
    private EventStatus status = EventStatus.SCHEDULED;
}
