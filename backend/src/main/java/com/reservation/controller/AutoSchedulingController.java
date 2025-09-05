package com.reservation.controller;

import com.reservation.service.AutoSchedulingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduling")
@RequiredArgsConstructor
public class AutoSchedulingController {

    private final AutoSchedulingService autoSchedulingService;

    @PostMapping("/generate/{templateId}")
    public ResponseEntity<AutoSchedulingService.SchedulingResult> generateSchedule(@PathVariable Long templateId) {
        try {
            AutoSchedulingService.SchedulingResult result = autoSchedulingService.generateSchedule(templateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            AutoSchedulingService.SchedulingResult errorResult = new AutoSchedulingService.SchedulingResult();
            errorResult.setTemplateId(templateId);
            errorResult.setSuccess(false);
            return ResponseEntity.badRequest().body(errorResult);
        }
    }
}
