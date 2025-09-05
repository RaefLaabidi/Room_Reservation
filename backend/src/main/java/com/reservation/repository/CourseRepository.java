package com.reservation.repository;

import com.reservation.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findBySubject(String subject);
    
    List<Course> findByDepartment(String department);
}
