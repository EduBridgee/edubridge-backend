package com.upc.edubridge.tutoring.controller;

import com.upc.edubridge.tutoring.model.TutoringSession;
import com.upc.edubridge.tutoring.service.TutoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Tutorías", description = "Endpoints para la gestión y programación de sesiones de tutoría entre docentes y alumnos")
public class TutoringController {

    @Autowired
    private TutoringService tutoringService;

    @Operation(
            summary = "Listar sesiones activas",
            description = "Retorna una lista de todas las tutorías programadas que no han sido canceladas ni han finalizado."
    )
    @GetMapping
    public List<TutoringSession> getAllSessions() {
        return tutoringService.listarActivas();
    }

    @Operation(
            summary = "Solicitar tutoría",
            description = "Permite a un estudiante solicitar una nueva sesión de tutoría especificando curso, fecha y docente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud creada exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = TutoringSession.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de la sesión inválidos")
    })
    @PostMapping("/request")
    public ResponseEntity<TutoringSession> requestTutoring(@RequestBody TutoringSession session) {
        return ResponseEntity.ok(tutoringService.crear(session));
    }

    @Operation(
            summary = "Cancelar tutoría",
            description = "Cambia el estado de una sesión de tutoría a 'Cancelada' mediante su identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cancelada correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró la sesión de tutoría")
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TutoringSession> cancel(
            @Parameter(description = "ID de la sesión de tutoría", example = "101") @PathVariable Long id) {
        return ResponseEntity.ok(tutoringService.cancelar(id));
    }

    @Operation(
            summary = "Reprogramar tutoría",
            description = "Actualiza la fecha y hora de una sesión existente. Requiere un JSON con el campo 'newDate' en formato ISO (ej. 2026-05-15T10:00:00Z)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reprogramación exitosa"),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido o error en la lógica de negocio")
    })
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<TutoringSession> reschedule(
            @Parameter(description = "ID de la sesión a reprogramar") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevo horario para la sesión",
                    content = @Content(schema = @Schema(example = "{\"newDate\": \"2026-05-20T15:30:00Z\"}"))
            )
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