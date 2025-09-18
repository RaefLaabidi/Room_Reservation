package com.reservation.dto.request;

import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUpdateRequest {
    
    private EventType type;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long roomId;
    private Long teacherId;
    private EventStatus status;
    private String title;
    private String description;
    private Integer expectedParticipants;

    // Manual getters since Lombok might not work at runtime
    public EventType getType() { return type; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Long getRoomId() { return roomId; }
    public Long getTeacherId() { return teacherId; }
    public EventStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getExpectedParticipants() { return expectedParticipants; }
    
    // Manual setters
    public void setType(EventType type) { this.type = type; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setExpectedParticipants(Integer expectedParticipants) { this.expectedParticipants = expectedParticipants; }
}
