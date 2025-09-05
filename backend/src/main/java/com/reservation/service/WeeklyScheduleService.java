package com.reservation.service;

import com.reservation.model.entity.*;
import com.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyScheduleService {

    private final ScheduleTemplateRepository scheduleTemplateRepository;
    private final TemplateCourseAssignmentRepository templateCourseAssignmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AutoSchedulingService autoSchedulingService;

    @Transactional
    public WeeklyScheduleRequest createWeeklySchedule(WeeklyScheduleRequest request) {
        log.info("Creating weekly schedule for week starting: {}", request.getWeekStartDate());
        
        // Create the schedule template
        ScheduleTemplate template = ScheduleTemplate.builder()
                .name(request.getTemplateName())
                .weekStartDate(request.getWeekStartDate())
                .weekEndDate(request.getWeekStartDate().plusDays(6)) // Week = 7 days
                .createdBy(userRepository.findById(request.getCreatedByUserId())
                    .orElseThrow(() -> new RuntimeException("User not found")))
                .status(ScheduleTemplate.ScheduleStatus.DRAFT)
                .build();

        ScheduleTemplate savedTemplate = scheduleTemplateRepository.save(template);
        
        // Create course assignments based on selected courses and priorities
        for (CourseAssignmentRequest courseRequest : request.getCourseAssignments()) {
            Course course = courseRepository.findById(courseRequest.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found: " + courseRequest.getCourseId()));
            
            TemplateCourseAssignment assignment = TemplateCourseAssignment.builder()
                    .template(savedTemplate)
                    .course(course)
                    .priority(courseRequest.getPriority())
                    .studentCount(courseRequest.getStudentCount())
                    .preferredTimeStart(courseRequest.getPreferredTimeStart())
                    .preferredTimeEnd(courseRequest.getPreferredTimeEnd())
                    .preferredDays(courseRequest.getPreferredDays())
                    .build();
            
            templateCourseAssignmentRepository.save(assignment);
        }
        
        // Run auto-scheduling
        try {
            AutoSchedulingService.SchedulingResult result = autoSchedulingService.generateSchedule(savedTemplate.getId());
            
            if (result.isSuccess()) {
                // Update template status to PUBLISHED
                savedTemplate.setStatus(ScheduleTemplate.ScheduleStatus.PUBLISHED);
                scheduleTemplateRepository.save(savedTemplate);
                log.info("Weekly schedule created successfully with {} courses scheduled", result.getScheduledCourses());
            } else {
                log.warn("Weekly schedule created with {} conflicts", result.getConflicts().size());
            }
            
            request.setSchedulingResult(result);
        } catch (Exception e) {
            log.error("Error during auto-scheduling: {}", e.getMessage());
            throw new RuntimeException("Failed to generate schedule: " + e.getMessage());
        }
        
        request.setTemplateId(savedTemplate.getId());
        return request;
    }

    public List<ScheduleTemplate> getWeeklySchedules() {
        return scheduleTemplateRepository.findAll();
    }

    public ScheduleTemplate getWeeklySchedule(Long templateId) {
        return scheduleTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Schedule template not found"));
    }

    @Transactional
    public void deleteWeeklySchedule(Long templateId) {
        if (!scheduleTemplateRepository.existsById(templateId)) {
            throw new RuntimeException("Schedule template not found");
        }
        templateCourseAssignmentRepository.deleteByTemplateId(templateId);
        scheduleTemplateRepository.deleteById(templateId);
    }

    // Data Transfer Objects
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WeeklyScheduleRequest {
        private Long templateId;
        private String templateName;
        private LocalDate weekStartDate;
        private Long createdByUserId;
        private List<CourseAssignmentRequest> courseAssignments;
        private AutoSchedulingService.SchedulingResult schedulingResult;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CourseAssignmentRequest {
        private Long courseId;
        private Integer priority; // 1 = highest priority
        private Integer studentCount;
        private java.time.LocalTime preferredTimeStart;
        private java.time.LocalTime preferredTimeEnd;
        private String preferredDays; // e.g., "1,3,5" for Mon,Wed,Fri
    }
}
