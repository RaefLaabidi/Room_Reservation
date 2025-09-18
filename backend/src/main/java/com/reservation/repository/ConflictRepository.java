package com.reservation.repository;

import com.reservation.model.entity.Conflict;
import com.reservation.model.entity.Event;
import com.reservation.model.enums.ConflictType;
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
    
    // Fetch conflicts with events eagerly loaded
    @Query("SELECT c FROM Conflict c LEFT JOIN FETCH c.event1 LEFT JOIN FETCH c.event2")
    List<Conflict> findAllWithEvents();
    
    // Check if conflict already exists - TEMPORARILY DISABLED
    // boolean existsByEvent1IdAndEvent2IdAndConflictType(Long event1Id, Long event2Id, ConflictType conflictType);
    
    // Find existing conflict - TEMPORARILY DISABLED  
    // Conflict findByEvent1IdAndEvent2IdAndConflictType(Long event1Id, Long event2Id, ConflictType conflictType);
}
