package com.reservation.repository;

import com.reservation.model.entity.TemplateCourseAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateCourseAssignmentRepository extends JpaRepository<TemplateCourseAssignment, Long> {
    
    List<TemplateCourseAssignment> findByTemplateId(Long templateId);
    
    void deleteByTemplateId(Long templateId);
}
