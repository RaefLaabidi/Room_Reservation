package com.reservation.controller;

import com.reservation.model.entity.ScheduleTemplate;
import com.reservation.repository.ScheduleTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-templates")
@RequiredArgsConstructor
public class ScheduleTemplateController {

    private final ScheduleTemplateRepository scheduleTemplateRepository;

    @GetMapping
    public ResponseEntity<List<ScheduleTemplate>> getAllTemplates() {
        return ResponseEntity.ok(scheduleTemplateRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleTemplate> getTemplateById(@PathVariable Long id) {
        return scheduleTemplateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ScheduleTemplate> createTemplate(@RequestBody ScheduleTemplate template) {
        ScheduleTemplate savedTemplate = scheduleTemplateRepository.save(template);
        return ResponseEntity.ok(savedTemplate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleTemplate> updateTemplate(@PathVariable Long id, @RequestBody ScheduleTemplate template) {
        if (!scheduleTemplateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        template.setId(id);
        ScheduleTemplate updatedTemplate = scheduleTemplateRepository.save(template);
        return ResponseEntity.ok(updatedTemplate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!scheduleTemplateRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        scheduleTemplateRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<ScheduleTemplate>> getTemplatesByStatus(@PathVariable ScheduleTemplate.ScheduleStatus status) {
        return ResponseEntity.ok(scheduleTemplateRepository.findByStatus(status));
    }
}
