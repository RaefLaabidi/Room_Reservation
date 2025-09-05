package com.reservation.repository;

import com.reservation.model.entity.TeacherSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, Long> {
    
    List<TeacherSubject> findByTeacherId(Long teacherId);
    
    List<TeacherSubject> findBySubject(String subject);
    
    @Query("SELECT ts FROM TeacherSubject ts WHERE ts.subject = :subject ORDER BY ts.expertiseLevel DESC")
    List<TeacherSubject> findBySubjectOrderByExpertiseDesc(@Param("subject") String subject);
}
