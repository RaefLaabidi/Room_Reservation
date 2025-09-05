package com.reservation.dto.response;

import com.reservation.model.enums.EventType;
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
public class UnscheduledEventResponse {
    private EventType type;
    private UserResponse teacher;
    private String title;
    private String description;
    private Integer expectedParticipants;
    private List<LocalDate> preferredDates;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;
    private String reason;
}
