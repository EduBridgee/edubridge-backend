package com.upc.edubridge.tutoring.controller;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.service.TutoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/tutoring")
@Tag(name = "Tutorías", description = "Endpoints para la gestión y programación de sesiones de tutoría entre docentes y alumnos")
public class TutoringController {

    @Autowired
    private TutoringService tutoringService;

    @Operation(summary = "Listar todas las sesiones", description = "Retorna una lista de todas las tutorías activas (no canceladas) registradas en el sistema.")
    @GetMapping
    public List<TutoringSession> getAllSessions() {
        return tutoringService.listarActivas();
    }

    @Operation(summary = "Solicitar tutoría", description = "Crea una nueva solicitud de tutoría. El estado inicial suele ser 'Pendiente'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutoría solicitada exitosamente",
                    content = @Content(schema = @Schema(implementation = TutoringSession.class))),
            @ApiResponse(responseCode = "400", description = "Datos de la solicitud inválidos")
    })
    @PostMapping("/request")
    public ResponseEntity<TutoringSession> requestTutoring(@RequestBody TutoringSession session) {
        return ResponseEntity.ok(tutoringService.crear(session));
    }

    @Operation(summary = "Aceptar tutoría", description = "Cambia el estado de una tutoría pendiente a 'Confirmada'.")
    @PatchMapping("/{id}/accept")
    public ResponseEntity<TutoringSession> accept(
            @Parameter(description = "ID de la tutoría a aceptar", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(tutoringService.aceptar(id));
    }

    @Operation(summary = "Finalizar tutoría", description = "Marca una sesión de tutoría como completada.")
    @PatchMapping("/{id}/finalize")
    public ResponseEntity<TutoringSession> finalize(
            @Parameter(description = "ID de la tutoría a finalizar", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(tutoringService.finalizar(id));
    }

    @Operation(summary = "Cancelar tutoría", description = "Anula una sesión de tutoría programada.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TutoringSession> cancel(
            @Parameter(description = "ID de la tutoría a cancelar", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(tutoringService.cancelar(id));
    }

    @Operation(
            summary = "Reprogramar tutoría",
            description = "Actualiza la fecha y hora de una sesión existente. Requiere un JSON con el campo 'newDate' en formato ISO."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tutoría reprogramada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<TutoringSession> reschedule(
            @Parameter(description = "ID de la sesión", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objeto con la nueva fecha",
                    content = @Content(schema = @Schema(example = "{\"newDate\": \"2026-05-15T10:00:00Z\"}"))
            ) @RequestBody Map<String, String> body) {
        try {
            String dateStr = body.get("newDate");
            java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(dateStr);
            LocalDateTime newDate = odt.toLocalDateTime();
            return ResponseEntity.ok(tutoringService.reprogramar(id, newDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Calificar tutoría", description = "Asigna una puntuación (estrellas) a una sesión de tutoría.")
    @PatchMapping("/{id}/rate")
    public ResponseEntity<TutoringSession> rate(
            @Parameter(description = "ID de la sesión", example = "1") @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer rating = body.get("rating");
            return ResponseEntity.ok(tutoringService.calificar(id, rating));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}