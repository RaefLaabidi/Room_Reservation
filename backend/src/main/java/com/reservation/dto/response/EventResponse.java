package com.reservation.dto.response;

import com.reservation.model.enums.EventStatus;
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
public class EventResponse {
    private Long id;
    private EventType type;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private RoomResponse room;
    private UserResponse teacher;
    private EventStatus status;
    private String title;
    private String description;
    private Integer expectedParticipants;
    private List<LocalDate> preferredDates;
    
    // Manual builder method in case Lombok fails
    public static EventResponse.EventResponseBuilder builder() {
        return new EventResponseBuilder();
    }
    
    public static class EventResponseBuilder {
        private Long id;
        private EventType type;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private RoomResponse room;
        private UserResponse teacher;
        private EventStatus status;
        private String title;
        private String description;
        private Integer expectedParticipants;
        private List<LocalDate> preferredDates;
        
        public EventResponseBuilder id(Long id) { this.id = id; return this; }
        public EventResponseBuilder type(EventType type) { this.type = type; return this; }
        public EventResponseBuilder date(LocalDate date) { this.date = date; return this; }
        public EventResponseBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public EventResponseBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public EventResponseBuilder room(RoomResponse room) { this.room = room; return this; }
        public EventResponseBuilder teacher(UserResponse teacher) { this.teacher = teacher; return this; }
        public EventResponseBuilder status(EventStatus status) { this.status = status; return this; }
        public EventResponseBuilder title(String title) { this.title = title; return this; }
        public EventResponseBuilder description(String description) { this.description = description; return this; }
        public EventResponseBuilder expectedParticipants(Integer expectedParticipants) { this.expectedParticipants = expectedParticipants; return this; }
        public EventResponseBuilder preferredDates(List<LocalDate> preferredDates) { this.preferredDates = preferredDates; return this; }
        
        public EventResponse build() {
            EventResponse response = new EventResponse();
            response.id = this.id;
            response.type = this.type;
            response.date = this.date;
            response.startTime = this.startTime;
            response.endTime = this.endTime;
            response.room = this.room;
            response.teacher = this.teacher;
            response.status = this.status;
            response.title = this.title;
            response.description = this.description;
            response.expectedParticipants = this.expectedParticipants;
            response.preferredDates = this.preferredDates;
            return response;
        }
    }
}
