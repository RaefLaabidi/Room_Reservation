package com.reservation.config;

import com.reservation.model.entity.User;
import com.reservation.model.entity.Room;
import com.reservation.model.entity.Event;
import com.reservation.model.enums.Role;
import com.reservation.model.enums.EventStatus;
import com.reservation.model.enums.EventType;
import com.reservation.repository.UserRepository;
import com.reservation.repository.RoomRepository;
import com.reservation.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        log.info("Initializing sample data...");

        // Create admin user
        User admin = User.builder()
                .name("Admin User")
                .email("admin@reservation.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // Create teacher user
        User teacher = User.builder()
                .name("John Teacher")
                .email("teacher@reservation.com")
                .password(passwordEncoder.encode("teacher123"))
                .role(Role.TEACHER)
                .build();
        userRepository.save(teacher);

        // Create student user
        User student = User.builder()
                .name("Jane Student")
                .email("student@reservation.com")
                .password(passwordEncoder.encode("student123"))
                .role(Role.STUDENT)
                .build();
        userRepository.save(student);

        // Test user with mypassword for debugging
        String testHash = passwordEncoder.encode("mypassword");
        System.out.println("=== DEBUG: BCrypt hash for 'mypassword': " + testHash);
        User testUser = User.builder()
                .name("Test User")
                .email("test@university.edu")
                .password(testHash)
                .role(Role.TEACHER)
                .build();
        userRepository.save(testUser);

        // Create sample rooms
        Room conferenceRoom = Room.builder()
                .name("Conference Room A")
                .capacity(20)
                .location("Building 1, Floor 2")
                .build();
        roomRepository.save(conferenceRoom);

        Room meetingRoom = Room.builder()
                .name("Meeting Room B")
                .capacity(8)
                .location("Building 1, Floor 3")
                .build();
        roomRepository.save(meetingRoom);

        Room boardroom = Room.builder()
                .name("Executive Boardroom")
                .capacity(12)
                .location("Building 2, Floor 5")
                .build();
        roomRepository.save(boardroom);

        Room trainingRoom = Room.builder()
                .name("Training Room C")
                .capacity(30)
                .location("Building 1, Floor 1")
                .build();
        roomRepository.save(trainingRoom);

        // Create sample events
        LocalDate today = LocalDate.now();
        
        Event event1 = Event.builder()
                .type(EventType.COURSE)
                .title("Java Programming Course")
                .description("Introduction to Java programming")
                .date(today.plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .room(conferenceRoom)
                .teacher(teacher)
                .status(EventStatus.SCHEDULED)
                .expectedParticipants(15)
                .build();
        eventRepository.save(event1);

        Event event2 = Event.builder()
                .type(EventType.MEETING)
                .title("Faculty Meeting")
                .description("Monthly faculty meeting")
                .date(today.plusDays(2))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(16, 0))
                .room(boardroom)
                .teacher(admin)
                .status(EventStatus.SCHEDULED)
                .expectedParticipants(8)
                .build();
        eventRepository.save(event2);

        Event event3 = Event.builder()
                .type(EventType.DEFENSE)
                .title("Thesis Defense")
                .description("Student thesis defense presentation")
                .date(today.plusDays(3))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .room(trainingRoom)
                .teacher(teacher)
                .status(EventStatus.SCHEDULED)
                .expectedParticipants(10)
                .build();
        eventRepository.save(event3);

        // Create a conflicting event for demonstration
        Event conflictEvent = Event.builder()
                .type(EventType.COURSE)
                .title("Python Programming Course")
                .description("Introduction to Python programming")
                .date(today.plusDays(1))
                .startTime(LocalTime.of(9, 30))
                .endTime(LocalTime.of(10, 30))
                .room(conferenceRoom)
                .teacher(admin)
                .status(EventStatus.SCHEDULED)
                .expectedParticipants(12)
                .build();
        eventRepository.save(conflictEvent);

        log.info("Sample data initialized successfully!");
        log.info("Admin user: admin@reservation.com / admin123");
        log.info("Teacher user: teacher@reservation.com / teacher123");
        log.info("Student user: student@reservation.com / student123");
    }
}
