package com.reservation.repository;

import com.reservation.model.entity.Availability;
import com.reservation.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByTeacher(User teacher);
    List<Availability> findByAvailableDate(LocalDate date);
    List<Availability> findByAvailableDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Availability a WHERE a.teacher = :teacher AND a.availableDate = :date AND " +
           "((a.startTime <= :startTime AND a.endTime >= :endTime))")
    List<Availability> findTeacherAvailability(@Param("teacher") User teacher,
                                             @Param("date") LocalDate date,
                                             @Param("startTime") LocalTime startTime,
                                             @Param("endTime") LocalTime endTime);
}
