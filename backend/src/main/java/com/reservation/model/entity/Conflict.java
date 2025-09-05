package com.reservation.model.entity;

import com.reservation.model.enums.ConflictType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conflicts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conflict {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConflictType conflictType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event1_id", nullable = false)
    private Event event1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event2_id")
    private Event event2;
    
    @Column(nullable = false)
    private String description;
    
    // Manual getters in case Lombok fails
    public Long getId() { return id; }
    public ConflictType getConflictType() { return conflictType; }
    public Event getEvent1() { return event1; }
    public Event getEvent2() { return event2; }
    public String getDescription() { return description; }
    
    // Manual setters
    public void setId(Long id) { this.id = id; }
    public void setConflictType(ConflictType conflictType) { this.conflictType = conflictType; }
    public void setEvent1(Event event1) { this.event1 = event1; }
    public void setEvent2(Event event2) { this.event2 = event2; }
    public void setDescription(String description) { this.description = description; }
    
    // Manual builder
    public static ConflictBuilder builder() {
        return new ConflictBuilder();
    }
    
    public static class ConflictBuilder {
        private Long id;
        private ConflictType conflictType;
        private Event event1;
        private Event event2;
        private String description;
        
        public ConflictBuilder id(Long id) { this.id = id; return this; }
        public ConflictBuilder conflictType(ConflictType conflictType) { this.conflictType = conflictType; return this; }
        public ConflictBuilder event1(Event event1) { this.event1 = event1; return this; }
        public ConflictBuilder event2(Event event2) { this.event2 = event2; return this; }
        public ConflictBuilder description(String description) { this.description = description; return this; }
        
        public Conflict build() {
            Conflict conflict = new Conflict();
            conflict.setId(this.id);
            conflict.setConflictType(this.conflictType);
            conflict.setEvent1(this.event1);
            conflict.setEvent2(this.event2);
            conflict.setDescription(this.description);
            return conflict;
        }
    }
}
