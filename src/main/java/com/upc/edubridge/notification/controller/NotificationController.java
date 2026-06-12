package com.upc.edubridge.notification.controller;

import com.upc.edubridge.notification.model.Notification;
import com.upc.edubridge.notification.repository.NotificationRepository;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notificaciones", description = "Endpoints para la gestión de alertas y mensajes del sistema")
@CrossOrigin(origins = "http://localhost:4200") 
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Operation(
            summary = "Enviar notificación",
            description = "Crea y envía una nueva alerta a un estudiante específico. Se utiliza para avisos generales o de rendimiento."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificación creada con éxito",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Notification.class)) }),
            @ApiResponse(responseCode = "400", description = "Datos de notificación inválidos")
    })
    @PostMapping
    public Notification sendNotification(@RequestBody Notification notification) {
        return notificationRepository.save(notification);
    }

    @Operation(
            summary = "Obtener notificaciones por estudiante",
            description = "Retorna la lista de alertas activas (no leídas) de un alumno ordenadas por fecha de creación descendente (las más recientes primero)."
    )
    @GetMapping("/student/{studentId}")
    public List<Notification> getMyNotifications(
            @Parameter(description = "ID del estudiante para filtrar sus alertas", example = "4")
            @PathVariable Long studentId) {
        
        return notificationRepository.findByStudentIdAndIsReadFalseOrderByCreatedAtDesc(studentId);
    }

    @Operation(
            summary = "Marcar como leída",
            description = "Actualiza el estado de una notificación específica a 'leída' mediante su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "La notificación no existe")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @Parameter(description = "ID único de la notificación", example = "10")
            @PathVariable Long id) {
        return notificationRepository.findById(id).map(notif -> {
            notif.setRead(true); 
            notificationRepository.save(notif); 

            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Notificación marcada como leída correctamente en Neon."
            ));
        }).orElse(ResponseEntity.notFound().build());
    }
}