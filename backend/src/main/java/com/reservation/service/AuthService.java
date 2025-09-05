package com.reservation.service;

import com.reservation.dto.request.LoginRequest;
import com.reservation.dto.request.RegisterRequest;
import com.reservation.dto.request.UserCreateRequest;
import com.reservation.dto.response.LoginResponse;
import com.reservation.dto.response.UserResponse;
import com.reservation.model.entity.User;
import com.reservation.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return LoginResponse.builder()
                .token(token)
                .user(userResponse)
                .build();
    }

    public UserResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create UserCreateRequest from RegisterRequest
        UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // This will be encoded in UserService
                .role(request.getRole())
                .build();

        return userService.createUser(userCreateRequest);
    }
}
