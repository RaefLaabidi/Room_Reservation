package com.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Long id;
    private String name;
    private int capacity;
    private String location;
    
    // Manual builder method in case Lombok fails
    public static RoomResponse.RoomResponseBuilder builder() {
        return new RoomResponseBuilder();
    }
    
    public static class RoomResponseBuilder {
        private Long id;
        private String name;
        private int capacity;
        private String location;
        
        public RoomResponseBuilder id(Long id) { this.id = id; return this; }
        public RoomResponseBuilder name(String name) { this.name = name; return this; }
        public RoomResponseBuilder capacity(int capacity) { this.capacity = capacity; return this; }
        public RoomResponseBuilder location(String location) { this.location = location; return this; }
        
        public RoomResponse build() {
            RoomResponse response = new RoomResponse();
            response.id = this.id;
            response.name = this.name;
            response.capacity = this.capacity;
            response.location = this.location;
            return response;
        }
    }
}
