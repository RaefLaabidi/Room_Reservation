package com.reservation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;
    
    @Column(name = "sessions_per_week")
    @Builder.Default
    private Integer sessionsPerWeek = 1;
    
    @Column(name = "min_capacity")
    @Builder.Default
    private Integer minCapacity = 1;
    
    @Column(name = "preferred_room_type")
    private String preferredRoomType;
    
    @Column(name = "department")
    private String department;
}
