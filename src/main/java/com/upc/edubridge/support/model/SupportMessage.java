package com.upc.edubridge.support.controller;

import com.upc.edubridge.support.model.SupportMessage;
import com.upc.edubridge.support.model.SupportTicket;
import com.upc.edubridge.support.repository.SupportMessageRepository;
import com.upc.edubridge.support.repository.SupportTicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/support")
@Tag(name = "Soporte", description = "Endpoints para la gestión de tickets de derivación a soporte")
@RequiredArgsConstructor
public class SupportController {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;

    @Operation(
            summary = "Crear un nuevo ticket de soporte",
            description = "Registra un caso de derivación con estado inicial PENDIENTE y fecha actual de creación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket creado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SupportTicket.class)) })
    })
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        ticket.setStatus("PENDIENTE");
        ticket.setCreatedAt(LocalDateTime.now());
        SupportTicket savedTicket = ticketRepository.save(ticket);
        return ResponseEntity.ok(savedTicket);
    }

    @Operation(
            summary = "Obtener tickets de un estudiante",
            description = "Retorna la lista de tickets de soporte creados por un estudiante específico, ordenados por fecha descendente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SupportTicket.class))) })
    })
    @GetMapping("/tickets/student/{studentId}")
    public ResponseEntity<List<SupportTicket>> getStudentTickets(
            @Parameter(description = "ID del estudiante", required = true) @PathVariable Long studentId) {
        return ResponseEntity.ok(ticketRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
    }

    @Operation(
            summary = "Obtener tickets asignados a un docente",
            description = "Retorna la lista de tickets de soporte derivados al docente conectado, ordenados por fecha descendente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida exitosamente",
                    content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SupportTicket.class))) })
    })
    @GetMapping("/tickets/teacher/{teacherId}")
    public ResponseEntity<List<SupportTicket>> getTeacherTickets(
            @Parameter(description = "ID del docente", required = true) @PathVariable Long teacherId) {
        return ResponseEntity.ok(ticketRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId));
    }

    @Operation(
            summary = "Agregar un mensaje al ticket",
            description = "Añade un nuevo mensaje a la conversación del ticket de soporte y actualiza el estado del ticket a RESPONDIDO (si es del docente) o PENDIENTE (si es del alumno)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje enviado y ticket actualizado exitosamente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = SupportMessage.class)) }),
            @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<SupportMessage> addMessage(
            @Parameter(description = "ID del ticket", required = true) @PathVariable Long ticketId,
            @RequestBody SupportMessage message) {
        
        return ticketRepository.findById(ticketId).map(ticket -> {
            message.setTicket(ticket);
            message.setCreatedAt(LocalDateTime.now());
            SupportMessage savedMessage = messageRepository.save(message);

            if (message.getSenderRole().equalsIgnoreCase("DOCENTE")) {
                ticket.setStatus("RESPONDIDO");
            } else {
                ticket.setStatus("PENDIENTE");
            }
            ticketRepository.save(ticket);

            return ResponseEntity.ok(savedMessage);
        }).orElse(ResponseEntity.notFound().build());
    }
}
