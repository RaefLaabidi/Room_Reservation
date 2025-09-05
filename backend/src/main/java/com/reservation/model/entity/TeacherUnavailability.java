package com.reservation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_unavailability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherUnavailability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;
    
    @Column(name = "unavailable_date", nullable = false)
    private LocalDate unavailableDate;
    
    @Column(name = "start_time")
    private LocalTime startTime;
    
    @Column(name = "end_time")
    private LocalTime endTime;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "all_day")
    @Builder.Default
    private Boolean allDay = false;
}
