package com.reservation.repository;

import com.reservation.model.entity.ScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleTemplateRepository extends JpaRepository<ScheduleTemplate, Long> {
    
    List<ScheduleTemplate> findByCreatedById(Long createdById);
    
    List<ScheduleTemplate> findByStatus(ScheduleTemplate.ScheduleStatus status);
    
    List<ScheduleTemplate> findByWeekStartDateBetween(LocalDate startDate, LocalDate endDate);
}
