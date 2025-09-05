package com.reservation.controller;

import com.reservation.dto.response.ConflictResponse;
import com.reservation.service.ConflictDetectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<ConflictResponse>> detectConflicts() {
        List<ConflictResponse> conflicts = conflictDetectionService.detectAndSaveAllConflicts();
        return ResponseEntity.ok(conflicts);
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
}
