package com.upc.edubridge.teacher.controller;

import com.upc.edubridge.teacher.model.StudentParticipation;
import com.upc.edubridge.teacher.repository.StudentParticipationRepository;
import com.upc.edubridge.student.repository.StudentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/participations")
@Tag(name = "Participación y Conducta", description = "Endpoints para el registro de puntos y observaciones conductuales (US07)")
public class StudentParticipationController {

    @Autowired
    private StudentParticipationRepository participationRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Operation(
            summary = "Registrar participación o conducta",
            description = "Permite a los docentes asignar puntos y observaciones a un estudiante. Se utiliza para el seguimiento conductual diario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participación registrada con éxito",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = StudentParticipation.class)) }),
            @ApiResponse(responseCode = "404", description = "Estudiante no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error al procesar el registro")
    })
    @PostMapping
    public StudentParticipation save(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos de la participación",
                    content = @Content(schema = @Schema(example = "{\"studentId\": 4, \"points\": 2, \"observation\": \"Participación activa en clase de algoritmos\", \"registrationDate\": \"2026-05-09\"}"))
            )
            @RequestBody Map<String, Object> payload) {

        Long studentId = Long.valueOf(payload.get("studentId").toString());

        var student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        StudentParticipation p = StudentParticipation.builder()
                .student(student)
                .points(Integer.parseInt(payload.get("points").toString()))
                .observation(payload.get("observation").toString())
                .registrationDate(LocalDateTime.parse(payload.get("registrationDate").toString() + "T00:00:00"))
                .build();

        return participationRepository.save(p);
    }
}