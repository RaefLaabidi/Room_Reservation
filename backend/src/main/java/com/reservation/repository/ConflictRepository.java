package com.reservation.repository;

import com.reservation.model.entity.Conflict;
import com.reservation.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConflictRepository extends JpaRepository<Conflict, Long> {
    List<Conflict> findByEvent1(Event event1);
    List<Conflict> findByEvent2(Event event2);
    
    @Query("SELECT c FROM Conflict c WHERE c.event1 = :event OR c.event2 = :event")
    List<Conflict> findByEvent(@Param("event") Event event);
}
