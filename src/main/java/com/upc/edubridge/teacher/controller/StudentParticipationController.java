package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.StudentParticipation;
import com.upc.edubridge.teacher.repository.StudentParticipationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@Tag(name = "Participación y Conducta", description = "Endpoints para la consulta de observaciones conductuales (US07)")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentParticipationController {

    @Autowired
    private StudentParticipationRepository participationRepository;

    @Operation(
            summary = "Obtener historial de participaciones por estudiante",
            description = "Retorna la lista completa de observaciones e intervenciones asignadas a un alumno específico para renderizar en su panel."
    )
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentParticipation>> getParticipationsByStudent(@PathVariable Long studentId) {
        List<StudentParticipation> historial = participationRepository.findByStudentIdOrderByRegistrationDateDesc(studentId);
        return ResponseEntity.ok(historial);
    }
}