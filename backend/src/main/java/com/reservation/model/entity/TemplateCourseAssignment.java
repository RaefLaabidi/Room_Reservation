package com.reservation.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "template_course_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateCourseAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ScheduleTemplate template;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_teacher_id")
    private User assignedTeacher; // Can be null for auto-assignment
    
    @Column(name = "preferred_time_start")
    private LocalTime preferredTimeStart;
    
    @Column(name = "preferred_time_end")
    private LocalTime preferredTimeEnd;
    
    @Column(name = "preferred_days")
    private String preferredDays; // e.g., "1,3,5" for Mon,Wed,Fri
    
    @Column(name = "student_count")
    private Integer studentCount;
    
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 1;
}
