package com.reservation.model.entity;

import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    // NEW: Optional relationship to course (for course-type events)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
    
    @Column
    private String title;
    
    @Column
    private String description;
    
    @Column
    private Integer expectedParticipants;
    
    // Google Calendar Integration
    @Column(name = "google_event_id", unique = true)
    private String googleEventId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_preferred_dates", 
                     joinColumns = @JoinColumn(name = "event_id"),
                     foreignKey = @ForeignKey(name = "fk_event_preferred_dates"))
    @Column(name = "preferred_date")
    private List<LocalDate> preferredDates;
    
    // Manual getters in case Lombok fails
    public Long getId() { return id; }
    public EventType getType() { return type; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Room getRoom() { return room; }
    public User getTeacher() { return teacher; }
    public Course getCourse() { return course; }
    public EventStatus getStatus() { return status; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getExpectedParticipants() { return expectedParticipants; }
    public List<LocalDate> getPreferredDates() { return preferredDates; }
    
    public void setId(Long id) { this.id = id; }
    public void setType(EventType type) { this.type = type; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public void setRoom(Room room) { this.room = room; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public void setCourse(Course course) { this.course = course; }
    public void setStatus(EventStatus status) { this.status = status; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setExpectedParticipants(Integer expectedParticipants) { this.expectedParticipants = expectedParticipants; }
    public void setPreferredDates(List<LocalDate> preferredDates) { this.preferredDates = preferredDates; }
}
