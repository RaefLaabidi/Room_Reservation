package com.reservation.dto.response;

import com.reservation.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    
    // Manual builder method in case Lombok fails
    public static UserResponse.UserResponseBuilder builder() {
        return new UserResponseBuilder();
    }
    
    public static class UserResponseBuilder {
        private Long id;
        private String name;
        private String email;
        private Role role;
        
        public UserResponseBuilder id(Long id) { this.id = id; return this; }
        public UserResponseBuilder name(String name) { this.name = name; return this; }
        public UserResponseBuilder email(String email) { this.email = email; return this; }
        public UserResponseBuilder role(Role role) { this.role = role; return this; }
        
        public UserResponse build() {
            UserResponse response = new UserResponse();
            response.id = this.id;
            response.name = this.name;
            response.email = this.email;
            response.role = this.role;
            return response;
        }
    }
}
