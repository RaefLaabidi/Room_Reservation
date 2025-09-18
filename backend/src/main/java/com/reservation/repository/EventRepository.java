package com.reservation.repository;

import com.reservation.model.entity.Event;
import com.reservation.model.entity.Room;
import com.reservation.model.entity.User;
import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByTeacher(User teacher);
    List<Event> findByRoom(Room room);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByType(EventType type);
    List<Event> findByDate(LocalDate date);
    List<Event> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    // NEW: Find events by course (after migration)
    // List<Event> findByCourseId(Long courseId);
    // @Query("SELECT e FROM Event e WHERE e.course.subject = :subject")
    // List<Event> findByCourseSubject(@Param("subject") String subject);
    
    boolean existsByRoomId(Long roomId);
    
    @Query("SELECT e FROM Event e WHERE e.room = :room AND e.date = :date AND " +
           "((e.startTime < :endTime AND e.endTime > :startTime))")
    List<Event> findConflictingEvents(@Param("room") Room room, 
                                     @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime);
    
    @Query("SELECT e FROM Event e WHERE e.room = :room AND e.date = :date AND " +
           "e.id != :excludeEventId AND " +
           "((e.startTime < :endTime AND e.endTime > :startTime))")
    List<Event> findConflictingEventsExcluding(@Param("room") Room room, 
                                               @Param("date") LocalDate date,
                                               @Param("startTime") LocalTime startTime,
                                               @Param("endTime") LocalTime endTime,
                                               @Param("excludeEventId") Long excludeEventId);
    
    @Query("SELECT e FROM Event e WHERE e.teacher.id = :teacherId AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findEventsByTeacherAndDateRange(@Param("teacherId") Long teacherId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Event e WHERE e.room.id = :roomId AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findEventsByRoomAndDateRange(@Param("roomId") Long roomId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.room LEFT JOIN FETCH e.course")
    List<Event> findAllWithRoomAndCourse();
    
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.room r LEFT JOIN FETCH e.course c")
    List<Event> findAllForAnalytics();
    
    // Google Calendar Integration
    Optional<Event> findByGoogleEventId(String googleEventId);
    
    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :startTime AND :endTime")
    List<Event> findByStartTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);
                                      
    // Professor and Student Schedule queries
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.teacher t WHERE t.email = :email AND e.date BETWEEN :startDate AND :endDate")
    List<Event> findByProfessorEmailAndDateBetween(@Param("email") String email, 
                                                   @Param("startDate") LocalDate startDate, 
                                                   @Param("endDate") LocalDate endDate);
    
    // For now, students will see all events (we can refine this later when we have proper enrollment tables)
    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.course c LEFT JOIN FETCH e.teacher t WHERE e.date BETWEEN :startDate AND :endDate")
    List<Event> findByStudentEmailAndDateBetween(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
}
