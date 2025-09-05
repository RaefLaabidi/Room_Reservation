package com.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCreateRequest {
    
    @NotBlank(message = "Room name is required")
    private String name;
    
    @Min(value = 1, message = "Capacity must be at least 1")
    private int capacity;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    // Manual getters in case Lombok fails
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public String getLocation() { return location; }
    
    public void setName(String name) { this.name = name; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setLocation(String location) { this.location = location; }
}
