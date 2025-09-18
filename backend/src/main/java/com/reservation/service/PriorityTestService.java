package com.reservation.service;

import com.reservation.model.entity.Course;
import com.reservation.model.entity.ScheduleTemplate;
import com.reservation.model.entity.TemplateCourseAssignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class PriorityTestService {

    // Uncomment this method to run a priority test on startup
    @PostConstruct
    public void testPrioritySystem() {
        log.info("🧪 TESTING PRIORITY SYSTEM");
        
        // Create a mock template with courses having different priorities
        ScheduleTemplate template = new ScheduleTemplate();
        template.setId(999L);
        template.setName("Priority Test Template");
        template.setWeekStartDate(LocalDate.now().with(java.time.DayOfWeek.MONDAY));
        template.setWeekEndDate(LocalDate.now().with(java.time.DayOfWeek.FRIDAY));
        
        List<TemplateCourseAssignment> assignments = new ArrayList<>();
        
        // Course with Priority 1 (should be scheduled last)
        TemplateCourseAssignment lowPriority = new TemplateCourseAssignment();
        Course course1 = new Course();
        course1.setId(1L);
        course1.setName("Low Priority Course");
        course1.setSubject("General Studies");
        course1.setDurationHours(2);
        lowPriority.setCourse(course1);
        lowPriority.setPriority(1);
        lowPriority.setStudentCount(20);
        assignments.add(lowPriority);
        
        // Course with Priority 5 (should be scheduled first)
        TemplateCourseAssignment highPriority = new TemplateCourseAssignment();
        Course course2 = new Course();
        course2.setId(2L);
        course2.setName("High Priority Course");
        course2.setSubject("Computer Science");
        course2.setDurationHours(2);
        highPriority.setCourse(course2);
        highPriority.setPriority(5);
        highPriority.setStudentCount(25);
        assignments.add(highPriority);
        
        // Course with Priority 3 (should be scheduled middle)
        TemplateCourseAssignment mediumPriority = new TemplateCourseAssignment();
        Course course3 = new Course();
        course3.setId(3L);
        course3.setName("Medium Priority Course");
        course3.setSubject("Mathematics");
        course3.setDurationHours(2);
        mediumPriority.setCourse(course3);
        mediumPriority.setPriority(3);
        mediumPriority.setStudentCount(30);
        assignments.add(mediumPriority);
        
        template.setCourseAssignments(assignments);
        
        log.info("🧪 Testing priority sorting with courses:");
        log.info("  - {} (Priority: {})", course1.getName(), lowPriority.getPriority());
        log.info("  - {} (Priority: {})", course2.getName(), highPriority.getPriority());
        log.info("  - {} (Priority: {})", course3.getName(), mediumPriority.getPriority());
        
        // Test the sorting logic directly
        List<TemplateCourseAssignment> sorted = assignments.stream()
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();
                
        log.info("🧪 Expected sorting order (highest priority first):");
        for (int i = 0; i < sorted.size(); i++) {
            TemplateCourseAssignment assignment = sorted.get(i);
            log.info("  {}. {} (Priority: {})", 
                    i + 1, 
                    assignment.getCourse().getName(), 
                    assignment.getPriority());
        }
        
        log.info("🧪 Priority test completed - check logs above to verify sorting works!");
    }
}