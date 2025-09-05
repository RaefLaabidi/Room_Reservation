package com.reservation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AdminController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/update-room-locations")
    public ResponseEntity<String> updateRoomLocations() {
        try {
            log.info("🔄 Starting room location updates...");
            
            // Update Computer Science & Technology Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Computer Lab Building - Programming Lab' WHERE id = 1");
            jdbcTemplate.update("UPDATE rooms SET location = 'Computer Science Building - Tech Center' WHERE id = 2");
            jdbcTemplate.update("UPDATE rooms SET location = 'Engineering Building - Computer Workshop' WHERE id = 3");
            jdbcTemplate.update("UPDATE rooms SET location = 'Tech Center - Advanced Computing Lab' WHERE id = 4");
            jdbcTemplate.update("UPDATE rooms SET location = 'IT Building - Software Development Lab' WHERE id = 5");

            // Mathematics Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Mathematics Building - Lecture Hall' WHERE id = 6");
            jdbcTemplate.update("UPDATE rooms SET location = 'Science Building - Math Classroom' WHERE id = 7");
            jdbcTemplate.update("UPDATE rooms SET location = 'Academic Center - Mathematics Seminar Room' WHERE id = 8");

            // Physics & Chemistry Laboratory Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Science Building - Physics Lab' WHERE id = 9");
            jdbcTemplate.update("UPDATE rooms SET location = 'Science Building - Chemistry Lab' WHERE id = 10");
            jdbcTemplate.update("UPDATE rooms SET location = 'Research Center - Advanced Science Lab' WHERE id = 11");
            jdbcTemplate.update("UPDATE rooms SET location = 'Physics Building - Experiment Lab' WHERE id = 12");
            jdbcTemplate.update("UPDATE rooms SET location = 'Chemistry Building - Research Lab' WHERE id = 13");

            // Business & Economics Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Business Building - Conference Room' WHERE id = 14");
            jdbcTemplate.update("UPDATE rooms SET location = 'Business Center - Executive Boardroom' WHERE id = 15");
            jdbcTemplate.update("UPDATE rooms SET location = 'Management Building - Seminar Hall' WHERE id = 16");
            jdbcTemplate.update("UPDATE rooms SET location = 'Business Building - Case Study Room' WHERE id = 17");
            jdbcTemplate.update("UPDATE rooms SET location = 'Economics Department - Lecture Hall' WHERE id = 18");

            // Engineering Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Engineering Building - Workshop Lab' WHERE id = 19");
            jdbcTemplate.update("UPDATE rooms SET location = 'Engineering Center - Mechanical Lab' WHERE id = 20");
            jdbcTemplate.update("UPDATE rooms SET location = 'Tech Building - Engineering Design Studio' WHERE id = 21");
            jdbcTemplate.update("UPDATE rooms SET location = 'Engineering Building - Electrical Lab' WHERE id = 22");

            // Language & Literature Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Humanities Building - Language Lab' WHERE id = 23");
            jdbcTemplate.update("UPDATE rooms SET location = 'Liberal Arts Building - Literature Seminar Room' WHERE id = 24");
            jdbcTemplate.update("UPDATE rooms SET location = 'Language Center - Communication Lab' WHERE id = 25");
            jdbcTemplate.update("UPDATE rooms SET location = 'Foreign Languages Building - Conversation Room' WHERE id = 26");

            // Psychology & Social Science Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Social Sciences Building - Psychology Lab' WHERE id = 27");
            jdbcTemplate.update("UPDATE rooms SET location = 'Psychology Department - Research Room' WHERE id = 28");
            jdbcTemplate.update("UPDATE rooms SET location = 'Social Studies Building - Sociology Seminar Room' WHERE id = 29");
            jdbcTemplate.update("UPDATE rooms SET location = 'Anthropology Building - Research Lab' WHERE id = 30");

            // Art & Design Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Arts Building - Creative Studio' WHERE id = 31");
            jdbcTemplate.update("UPDATE rooms SET location = 'Design Center - Art Workshop' WHERE id = 32");
            jdbcTemplate.update("UPDATE rooms SET location = 'Creative Arts Building - Digital Media Studio' WHERE id = 33");
            jdbcTemplate.update("UPDATE rooms SET location = 'Art Department - Photography Lab' WHERE id = 34");

            // Medical & Health Science Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Medical Building - Anatomy Lab' WHERE id = 35");
            jdbcTemplate.update("UPDATE rooms SET location = 'Health Sciences Building - Clinical Room' WHERE id = 36");
            jdbcTemplate.update("UPDATE rooms SET location = 'Medical Center - Research Lab' WHERE id = 37");
            jdbcTemplate.update("UPDATE rooms SET location = 'Health Department - Public Health Seminar Room' WHERE id = 38");

            // Environmental Science Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Environmental Sciences Building - Green Lab' WHERE id = 39");
            jdbcTemplate.update("UPDATE rooms SET location = 'Sustainability Center - Environment Research Lab' WHERE id = 40");
            jdbcTemplate.update("UPDATE rooms SET location = 'Environmental Studies - Field Research Room' WHERE id = 41");

            // General Purpose Rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Main Building - Lecture Hall A' WHERE id = 42");
            jdbcTemplate.update("UPDATE rooms SET location = 'Academic Center - Classroom 201' WHERE id = 43");
            jdbcTemplate.update("UPDATE rooms SET location = 'Main Building - Lecture Hall B' WHERE id = 44");
            jdbcTemplate.update("UPDATE rooms SET location = 'Academic Building - General Classroom' WHERE id = 45");

            // Additional general rooms
            jdbcTemplate.update("UPDATE rooms SET location = 'Main Building - Lecture Hall C' WHERE id > 45 AND id <= 50");
            jdbcTemplate.update("UPDATE rooms SET location = 'Academic Center - General Classroom' WHERE id > 50 AND id <= 55");
            jdbcTemplate.update("UPDATE rooms SET location = 'Campus Center - Multi-Purpose Room' WHERE id > 55");

            log.info("✅ Room location updates completed successfully!");
            
            return ResponseEntity.ok("🎯 Room locations updated successfully! AI algorithm will now make precise room assignments.");
            
        } catch (Exception e) {
            log.error("❌ Error updating room locations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating room locations: " + e.getMessage());
        }
    }

    @GetMapping("/verify-room-locations")
    public ResponseEntity<List<Map<String, Object>>> verifyRoomLocations() {
        try {
            log.info("🔍 Verifying room locations...");
            
            String sql = "SELECT id, name, capacity, location FROM rooms ORDER BY id";
            List<Map<String, Object>> rooms = jdbcTemplate.queryForList(sql);
            
            log.info("📊 Found {} rooms with updated locations", rooms.size());
            
            return ResponseEntity.ok(rooms);
            
        } catch (Exception e) {
            log.error("❌ Error verifying room locations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}
