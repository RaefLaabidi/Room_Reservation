package com.reservation.repository;

import com.reservation.model.entity.TeacherAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherAvailabilityRepository extends JpaRepository<TeacherAvailability, Long> {
    
    List<TeacherAvailability> findByTeacherId(Long teacherId);
    
    List<TeacherAvailability> findByTeacherIdAndDayOfWeek(Long teacherId, Integer dayOfWeek);
    
    @Query("SELECT ta FROM TeacherAvailability ta WHERE ta.teacher.id = :teacherId AND ta.dayOfWeek = :dayOfWeek AND ta.isAvailable = true")
    List<TeacherAvailability> findAvailableSlots(@Param("teacherId") Long teacherId, @Param("dayOfWeek") Integer dayOfWeek);
}
