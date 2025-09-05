package com.reservation.dto.response;

import com.reservation.model.enums.ConflictType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConflictResponse {
    private Long id;
    private ConflictType conflictType;
    private EventResponse event1;
    private EventResponse event2;
    private String description;
    
    // Manual getters in case Lombok fails
    public Long getId() { return id; }
    public ConflictType getConflictType() { return conflictType; }
    public EventResponse getEvent1() { return event1; }
    public EventResponse getEvent2() { return event2; }
    public String getDescription() { return description; }
    
    // Manual setters
    public void setId(Long id) { this.id = id; }
    public void setConflictType(ConflictType conflictType) { this.conflictType = conflictType; }
    public void setEvent1(EventResponse event1) { this.event1 = event1; }
    public void setEvent2(EventResponse event2) { this.event2 = event2; }
    public void setDescription(String description) { this.description = description; }
    
    // Manual builder
    public static ConflictResponseBuilder builder() {
        return new ConflictResponseBuilder();
    }
    
    public static class ConflictResponseBuilder {
        private Long id;
        private ConflictType conflictType;
        private EventResponse event1;
        private EventResponse event2;
        private String description;
        
        public ConflictResponseBuilder id(Long id) { this.id = id; return this; }
        public ConflictResponseBuilder conflictType(ConflictType conflictType) { this.conflictType = conflictType; return this; }
        public ConflictResponseBuilder event1(EventResponse event1) { this.event1 = event1; return this; }
        public ConflictResponseBuilder event2(EventResponse event2) { this.event2 = event2; return this; }
        public ConflictResponseBuilder description(String description) { this.description = description; return this; }
        
        public ConflictResponse build() {
            ConflictResponse response = new ConflictResponse();
            response.setId(this.id);
            response.setConflictType(this.conflictType);
            response.setEvent1(this.event1);
            response.setEvent2(this.event2);
            response.setDescription(this.description);
            return response;
        }
    }
}
