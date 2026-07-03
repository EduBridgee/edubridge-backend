package com.upc.edubridge.support.controller;

import com.upc.edubridge.support.model.SupportMessage;
import com.upc.edubridge.support.model.SupportTicket;
import com.upc.edubridge.support.repository.SupportMessageRepository;
import com.upc.edubridge.support.repository.SupportTicketRepository;
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

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicket> createTicket(@RequestBody SupportTicket ticket) {
        ticket.setStatus("PENDIENTE");
        ticket.setCreatedAt(LocalDateTime.now());
        SupportTicket savedTicket = ticketRepository.save(ticket);
        return ResponseEntity.ok(savedTicket);
    }

    @GetMapping("/tickets/student/{studentId}")
    public ResponseEntity<List<SupportTicket>> getStudentTickets(@PathVariable Long studentId) {
        return ResponseEntity.ok(ticketRepository.findByStudentIdOrderByCreatedAtDesc(studentId));
    }

    @GetMapping("/tickets/teacher/{teacherId}")
    public ResponseEntity<List<SupportTicket>> getTeacherTickets(@PathVariable Long teacherId) {
        return ResponseEntity.ok(ticketRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId));
    }

    @PostMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<SupportMessage> addMessage(
            @PathVariable Long ticketId,
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
