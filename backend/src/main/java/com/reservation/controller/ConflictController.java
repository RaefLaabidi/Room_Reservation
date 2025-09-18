package com.reservation.controller;

import com.reservation.dto.response.ConflictResponse;
import com.reservation.service.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conflicts")
@RequiredArgsConstructor
public class ConflictController {

    private final ConflictDetectionService conflictDetectionService;

    @GetMapping
    public ResponseEntity<List<ConflictResponse>> getAllConflicts() {
        List<ConflictResponse> conflicts = conflictDetectionService.getAllConflicts();
        return ResponseEntity.ok(conflicts);
    }

    @PostMapping("/detect")
    public ResponseEntity<?> detectConflicts() {
        try {
            System.out.println("=== CONFLICT DETECTION ENDPOINT CALLED ===");
            List<ConflictResponse> conflicts = conflictDetectionService.detectAndSaveAllConflicts();
            System.out.println("=== RETURNING " + conflicts.size() + " CONFLICTS ===");
            return ResponseEntity.ok(conflicts);
        } catch (Exception e) {
            System.err.println("Error in conflict detection endpoint: " + e.getMessage());
            e.printStackTrace();
            
            // Return detailed error information
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Conflict detection failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/preview")
    public ResponseEntity<List<ConflictResponse>> previewConflicts() {
        List<ConflictResponse> conflicts = conflictDetectionService.detectConflictsWithoutSaving();
        return ResponseEntity.ok(conflicts);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllConflicts() {
        conflictDetectionService.clearAllConflicts();
        return ResponseEntity.ok("All conflicts cleared");
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConflictDetection() {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("=== MANUAL CONFLICT TEST ===");
            List<ConflictResponse> conflicts = conflictDetectionService.testConflictDetection();
            result.put("success", true);
            result.put("conflictsFound", conflicts.size());
            result.put("conflicts", conflicts);
            result.put("message", "Conflict detection test completed successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Test conflict detection failed: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "Conflict detection test failed");
            return ResponseEntity.badRequest().body(result);
        }
    }
}
