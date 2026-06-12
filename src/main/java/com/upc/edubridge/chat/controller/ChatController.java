package com.upc.edubridge.chat.controller;

import com.upc.edubridge.chat.service.GeminiService;
import com.upc.edubridge.enrollment.model.Enrollment;
import com.upc.edubridge.enrollment.repository.EnrollmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Asistente AI", description = "Endpoints para la interacción con el chatbot inteligente de EduBridge")
@RequiredArgsConstructor 
public class ChatController {

    private final GeminiService geminiService;
    private final EnrollmentRepository enrollmentRepository; 

    @Operation(
            summary = "Consultar al asistente virtual",
            description = "Envía un mensaje al modelo de IA (Gemini) cruzando la información de matrículas real de Neon para evitar respuestas incoherentes."
    )
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> askAi(@RequestBody Map<String, String> payload) {
        String message = payload.get("message");
        String role = payload.get("role");
        String name = payload.get("userName");

        
        String userIdStr = payload.get("userId");
        String listaCursosValidada = "No especificados";

        if (userIdStr != null) {
            try {
                Long studentId = Long.valueOf(userIdStr);
                
                List<Enrollment> matriculasActivas = enrollmentRepository.findByStudentId(studentId);

                if (!matriculasActivas.isEmpty()) {
                    listaCursosValidada = matriculasActivas.stream()
                            .map(e -> e.getCourse().getName()) 
                            .collect(Collectors.joining(", "));
                }
            } catch (Exception e) {
                System.err.println("Error al recuperar asignaturas desde Neon: " + e.getMessage());
            }
        }

        String currentTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        
        String systemPrompt = String.format(
                "Eres el asistente oficial de EduBridge. Tu nombre es EduBridge AI. " +
                        "Usuario: %s | Rol: %s | Hora: %s. " +
                        "IMPORTANTE: El usuario está matriculado ÚNICAMENTE en estos cursos: [%s]. " +
                        "Si pregunta por sus cursos o intenta agendar una tutoría, contrasta rigurosamente contra esta lista. " +
                        "Responde de forma concisa, humana y evita discursos motivacionales largos.",
                name, role, currentTime, listaCursosValidada
        );

        String answer = geminiService.getAiResponse(systemPrompt, message);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}