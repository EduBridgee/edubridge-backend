package com.upc.edubridge.tutoring.controller;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.service.TutoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tutoring")
@CrossOrigin(origins = "http://localhost:4200")
public class TutoringController {

    @Autowired
    private TutoringService tutoringService;

    @GetMapping
    public List<TutoringSession> getAllSessions() {
        return tutoringService.listarActivas();
    }

    @PostMapping("/request")
    public ResponseEntity<TutoringSession> requestTutoring(@RequestBody TutoringSession session) {
        return ResponseEntity.ok(tutoringService.crear(session));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TutoringSession> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(tutoringService.cancelar(id));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<TutoringSession> reschedule(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        try {
            String dateStr = body.get("newDate");
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(dateStr);
            LocalDateTime newDate = odt.toLocalDateTime();

            return ResponseEntity.ok(tutoringService.reprogramar(id, newDate));
        } catch (Exception e) {
            System.err.println("Error parseando fecha: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}